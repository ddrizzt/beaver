package beaver;


import jodd.json.JsonArray;
import jodd.json.JsonObject;
import jodd.json.JsonParser;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
// This means that this class is a Controller
@RequestMapping(path = "/beaver")
// This means URL's start with /demo (after Application path)
public class MainController {
    private static final Logger lg = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private TemplatesRepository tlRepository;

    @Autowired
    private StacksRepository stRepository;

    @Autowired
    private StackLogsRepository stlRepository;

    //Sample: curl -X PUT -d 'parametsjson={"KeyName": "eason_personal_rsa"}'  http://localhost:8080/beaver/createstack/eason/e3/us-west-2/ec2_simple_demo
    @PutMapping(path = "/createstack/{user}/{stackname}/{availableregion}/{templatename}")
    @Transactional
    public @ResponseBody
    String createStacks(HttpServletRequest request, @PathVariable String user, @PathVariable String stackname, @PathVariable String availableregion, @PathVariable String templatename, @RequestParam(required = false) String parametsjson, @RequestParam(required = false) String comments) {
        String se = request.getSession().getId();
        lg.info(String.format("%s -- Request new st %s by %s on region %s", se, user, stackname, availableregion));
        Stacks st = new Stacks();
        List<Templates> tls = tlRepository.findByTemplatename(templatename);

        JsonObject json = new JsonObject();
        if (tls.isEmpty()) {
            json.put("Message", "Wrong template name!");
            return json.toString();

        } else if (tls.size() > 1) {
            json.put("Message", "Multiple templates found!");
            return json.toString();
        }
        Templates tl = tls.get(0);

        List<Stacks> sts = stRepository.findByStackname(stackname);
        if (!sts.isEmpty()) {
            lg.error(String.format("%s -- Same stack %s already existed!", se, stackname));
            json.put("Message", "Same stack already existed!");
            return json.toString();
        }

        st.setUsername(user);
        st.setStackname(stackname);
        st.setTemplateid(tls.get(0).getTemplateid());
        st.setStackarn("");
        st.setComments(comments);
        st.setParameters(parametsjson);
        st.setAvailableregion(availableregion);
        st.setStatus(DICT.CF_PENDING);
        st.setCreatedat(new Date(System.currentTimeMillis()));
        st.setUpdatedat(new Date(System.currentTimeMillis()));

        Map<String, Object> params = new HashMap<String, Object>();
        if (!StringUtil.isEmpty(parametsjson)) {
            params = new JsonParser().parse(parametsjson);
        }

        JsonObject resp = new JsonObject();
        CloudformationTracker clt = new CloudformationTracker(se, st, templatename, params, stlRepository, stRepository);
        st = clt.createStack();
        if (st.getStackid() == null || st.getStatus().equals(DICT.ERROR_ON_API)) {
            lg.error(String.format("%s -- Create stack failed! %s", se, st.getComments()));
            resp.put("Message", String.format("%s -- Create stack failed! %s", se, st.getComments()));
            return resp.toString();
        }
        CloudFormationHandler.getHander().submitTask(clt);

        lg.info(String.format("%s -- Stack Saved: %s, %s", se, st.getStackid(), st.getStackname()));
        return st.toJsonStr();
    }

    //Sample: curl -X DELETE http://localhost:8080/beaver/deletestack/user/stackname
    @DeleteMapping(path = "/deletestack/{user}/{stackname}")
    @Transactional
    public @ResponseBody
    String deleteStacks(HttpServletRequest request, @PathVariable String user, @PathVariable String stackname, @RequestBody(required = false) String comments) {
        JsonObject response = new JsonObject();
        String se = request.getSession().getId();
        lg.info(String.format("%s -- Request delete stack %s by %s", se, stackname, user));

        List<Stacks> sts = stRepository.findByStackname(stackname);
        if (sts.isEmpty()) {
            lg.info(String.format("%s -- No stack found: %s", se, stackname));
            response.put("Message", String.format("%s -- No stack found: %s", se, stackname));
            return response.toString();
        }

        Stacks st = sts.get(0);
        if (st.getStatus().equals(DICT.CF_DELETE_COMPLETE)) {
            lg.info(String.format("%s -- Stack had already been deleted. %s", se, stackname));
            response.put("Message", String.format("%s -- Stack %s had already been deleted. %s", se, stackname, st.getStatus()));
            return response.toString();
        }

        CloudformationTracker cft = new CloudformationTracker(se, st, stlRepository, stRepository);
        st = cft.deleteStack();
        if (st.getStatus().equals(DICT.ERROR_ON_API)) {
            lg.error(String.format("%s -- Delete stack failed. %s, %s, %s", se, stackname, st.getStatus(), st.getComments()));
            response.put("Message", String.format("%s -- Delete stack failed. %s, %s, %s", se, stackname, st.getStatus(), st.getComments()));
            return response.toString();
        }

        CloudFormationHandler.getHander().submitTask(cft);
        response.put("Message", "Stack is deleting now, need several minutes to complete...");

        return response.toString();
    }

    //Sample: http://localhost:8080/beaver/stacklogs/stackname/status
    @RequestMapping(value = {"/stacklogs/{stackname}/{status}", "/stacklogs/{stackname}"}, method = RequestMethod.GET, produces = "text/json; charset=utf-8")
    public @ResponseBody
    String getAllStacklogs(HttpServletRequest request, @PathVariable String stackname, @PathVariable(required = false) String status) {
        String se = request.getSession().getId();
        lg.info(String.format("%s -- Request stack logs %s, %s", se, stackname, status));
        List<Stacks> sts = stRepository.findByStackname(stackname);
        if (sts.isEmpty()) {
            lg.error(String.format("Wrong stack name: %s", se, stackname));
            JsonObject resp = new JsonObject();
            resp.put("Message", "Can't find your given stack info!");
            return resp.toString();
        }

        lg.info(String.format("%s -- Search stacklogs by name:%s, status: %s.", se, stackname, status));
        List<StackLogs> stls = null;
        if (status != null) {
            stls = stlRepository.findByStackidAndStatusOrderByCreatedatDesc(sts.get(0).getStackid(), status);
        } else {
            stls = stlRepository.findByStackidOrderByCreatedatDesc(sts.get(0).getStackid());
        }

        JsonArray jsonArray = new JsonArray();
        stls.forEach((stl) -> jsonArray.add(stl.toJson()));

        return jsonArray.toString();
    }

    //Sample: http://localhost:8080/beaver/stacks/stackname
    @RequestMapping(value = "/stacks/{stackname}", method = RequestMethod.GET, produces = "text/json; charset=utf-8")
    public @ResponseBody
    String getStackInfo(HttpServletRequest request, @PathVariable String stackname) {
        String se = request.getSession().getId();
        lg.info(String.format("%s -- Request stack info %s", se, stackname));
        List<Stacks> sts = stRepository.findByStackname(stackname);
        if (!sts.isEmpty()) {
            return sts.get(0).toJsonStr();
        } else {
            JsonObject resp = new JsonObject();
            resp.put("Message", "Can't find your given stack info!");
            return resp.toString();
        }
    }

    //Sample: http://localhost:8080/beaver/templates
    @GetMapping(path = "/templates")
    public @ResponseBody
    Iterable<Templates> getAllTemplates() {
        // This returns a JSON or XML with the users
        return tlRepository.findAll();
    }
}
