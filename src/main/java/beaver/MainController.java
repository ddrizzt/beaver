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
import java.io.IOException;
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

    //Sample: http://localhost:8080/beaver/createstack?stackname=abc&templatename=dovecot_director_demo&user=eason&availableregion=us-west-2&parametsjson=%7b%22KeyName%22%3a%22eason_personal_rsa%22%7d
    //Sample: http://localhost:8080/beaver/createstack?stackname=abc&templatename=kinesis_data_demo&user=eason&availableregion=us-west-2&parametsjson=%7b%22KeyName%22%3a%22eason_personal_rsa%22%2c+%22RedshiftRootUser%22%3a%22eason%22%7d
    @GetMapping(path = "/createstack")
    @PutMapping(path = "/createstack")
    @Transactional
    public @ResponseBody
    String createStacks(HttpServletRequest request, @RequestParam String user, @RequestParam String stackname, @RequestParam String availableregion, @RequestParam String templatename, @RequestParam(required = false) String parametsjson, @RequestParam(required = false) String comments) {
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
            lg.error(String.format("%s -- Same st %s already existed!", se, stackname));
            json.put("Message", "Same stack already existed!");
            return json.toString();
        }

        st.setUsername(user);
        st.setStackname(stackname);
        st.setTemplateid(tls.get(0).getTemplateid());
        st.setComments(comments);
        st.setParameters(parametsjson);
        st.setAvailableregion(availableregion);
        st.setStatus(DICT.CF_PENDING);
        st.setCreatedat(new Date(System.currentTimeMillis()));
        st.setUpdatedat(new Date(System.currentTimeMillis()));
        stRepository.save(st);

        StackLogs stl = new StackLogs();
        stl.setStackid(st.getStackid());
        stl.setUsername(user);
        stl.setStatus(DICT.CF_PENDING);
        stl.setCreatedat(new Date(System.currentTimeMillis()));
        stl.setUpdatedat(new Date(System.currentTimeMillis()));
        stlRepository.save(stl);

        Map<String, Object> params = new HashMap<String, Object>();
        if (!StringUtil.isEmpty(parametsjson)) {
            params = new JsonParser().parse(parametsjson);
        }

        try {
            CloudformationTracker clt = new CloudformationTracker(se, st, templatename, params, stlRepository, stRepository);
            CloudFormationHandler.getHander().submit(clt);
        } catch (IOException e) {
            lg.error(String.format("%s -- Fail to submit thread! %s", e.getMessage()));
        }


        lg.info(String.format("%s -- Stack Saved: %s, %s", se, st.getStackid(), st.getStackname()));
        return st.toJsonStr();
    }

    //Sample: http://localhost:8080/beaver/deletestack?stackname=abc&user=eason
    @GetMapping(path = "/deletestack")
    @Transactional
    public @ResponseBody
    String deleteStacks(HttpServletRequest request, @RequestParam String user, @RequestParam String stackname, @RequestParam(required = false) String comments) {
        JsonObject response = new JsonObject();
        String se = request.getSession().getId();
        lg.info(String.format("%s -- Request delete stack %s by %s", se, stackname, user));


        List<Stacks> sts = stRepository.findByStackname(stackname);
        if (sts.isEmpty()) {
            lg.info(String.format("%s -- No stack found: %s", se, stackname));
            response.put("Message", String.format("%s -- No stack found: %s", se, stackname));
            return response.toString();
        }

        CloudFormationHandler.getHander().submit(new CloudformationTracker(se, sts.get(0), stlRepository, stRepository));
        response.put("Message", "Stack is deleting now, need several minutes to complete...");

        return response.toString();
    }

    //Sample: http://localhost:8080/beaver/stacklogs?stackname=e1
    @RequestMapping(value = "/stacklogs", method = RequestMethod.GET, produces = "text/json; charset=utf-8")
    public @ResponseBody
    String getAllStacklogs(HttpServletRequest request, @RequestParam String stackname, @RequestParam(required = false) Integer status) {
        String se = request.getSession().getId();
        lg.info(String.format("%s -- Request stack logs %s, %s", se, stackname, status));
        List<Stacks> sts = stRepository.findByStackname(stackname);
        if (sts.isEmpty()) {
            lg.error("Wrong stack name: %s", se, stackname);
            JsonObject resp = new JsonObject();
            resp.put("Message", "Can't find your given stack info!");
            return resp.toString();
        }

        lg.info(String.format("%s -- Search stacklogs by name:%s, status:%s.", se, stackname, status));
        List<StackLogs> stls = null;
        if (status != null) {
            stls = stlRepository.findByStackidAndStatus(sts.get(0).getStackid(), status);
        } else {
            stls = stlRepository.findByStackid(sts.get(0).getStackid());
        }

        JsonArray jsonArray = new JsonArray();
        stls.forEach((stl) -> jsonArray.add(stl.toJson()));

        return jsonArray.toString();
    }

    //Sample: http://localhost:8080/beaver/stacks?stackname=e1
    @RequestMapping(value = "/stacks", method = RequestMethod.GET, produces = "text/json; charset=utf-8")
    public @ResponseBody
    String getStackInfo(HttpServletRequest request, @RequestParam String stackname) {
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
