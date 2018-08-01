package beaver;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.*;
import com.amazonaws.services.cloudformation.model.Stack;
import jodd.io.FileUtil;
import jodd.json.JsonArray;
import jodd.json.JsonObject;
import jodd.util.ArraysUtil;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

public class CloudformationTracker implements Callable<CloudformationTracker> {
    private static final Logger lg = LoggerFactory.getLogger(CloudFormationHandler.class);

    private StackLogsRepository stlRepository;

    private StacksRepository stRepository;

    private String se;
    private Stacks stacks;
    private String templatename;
    private File tempFile;
    private Map<String, Object> params;
    private AmazonCloudFormation cfBuilder;
    private boolean deleteOnly = false;


    public CloudformationTracker(String se, Stacks stacks, String templatename, Map<String, Object> params, StackLogsRepository stlRepository, StacksRepository stRepository) throws IOException {
        this.se = se;
        this.stacks = stacks;
        this.templatename = templatename;
        this.params = params;
        this.tempFile = new File(String.format("%s/src/main/resources/templates/%s.json", System.getenv("PROJECT_ROOT"), templatename));
        this.stlRepository = stlRepository;
        this.stRepository = stRepository;

        cfBuilder = new AmazonCloudFormationClient(new PropertiesFileCredentialsProvider(String.format("%s/src/main/resources/AwsCredentials.properties", System.getenv("PROJECT_ROOT"))));
        cfBuilder.setRegion(Region.getRegion(Regions.fromName(stacks.getAvailableregion())));

        lg.info(String.format("%s -- Initial cloud formation stack resources %s, template: %s, parameters: %s", se, stacks.getStackname(), templatename, params));
    }

    /**
     * This function for delete stack only!
     * @param se
     * @param stacks
     */
    public CloudformationTracker(String se, Stacks stacks, StackLogsRepository stlRepository, StacksRepository stRepository) {
        this.se = se;
        this.stacks = stacks;
        this.deleteOnly = true;
        this.stlRepository = stlRepository;
        this.stRepository = stRepository;
        cfBuilder = new AmazonCloudFormationClient(new PropertiesFileCredentialsProvider(String.format("%s/src/main/resources/AwsCredentials.properties", System.getenv("PROJECT_ROOT"))));
        cfBuilder.setRegion(Region.getRegion(Regions.fromName(stacks.getAvailableregion())));
        lg.info(String.format("%s -- Initial cloud formation stack resources %s, template: %s, parameters: %s", se, stacks.getStackname(), templatename, params));
    }

    @Override
    public CloudformationTracker call() {
        try {
            String stlogStatus = "";
            if (deleteOnly == false) {
                lg.info(String.format("%s -- Creating stack now....%s, %s", se, stacks.getStackname(), templatename));
                CreateStackRequest crReq = new CreateStackRequest();
                crReq.setStackName(stacks.getStackname());
                crReq.setTemplateBody(FileUtil.readString(tempFile));
                crReq.setCapabilities(Arrays.asList("CAPABILITY_IAM")); //TODO HERE

                List<Parameter> plist = new ArrayList<>();
                this.params.forEach((k, v) -> {
                    Parameter p = new Parameter();
                    p.setParameterKey(k);
                    p.setParameterValue(v.toString());
                    plist.add(p);
                });
                crReq.setParameters(plist);
                CreateStackResult rs = cfBuilder.createStack(crReq);

                lg.info(String.format("%s -- Stack created: %s, %s, %s", se, stacks.getStackname(), templatename, rs.getStackId()));
            } else {
                DeleteStackRequest delReq = new DeleteStackRequest();
                delReq.setStackName(stacks.getStackname());
                DeleteStackResult res = cfBuilder.deleteStack(delReq);
                lg.info(String.format("%s -- Delete stack %s", se, stacks.getStackname()));
            }

            while (!ArraysUtil.contains(new String[]{DICT.CF_CREATE_COMPLETE, DICT.CF_ROLLBACK_COMPLETE, DICT.CF_DELETE_COMPLETE}, stlogStatus)) {
                lg.info(String.format("%s -- Get stack newest status: %s, %s", se, stacks.getStackname(), stlogStatus));

                stlogStatus = reflushStackAndLogs(stlogStatus, deleteOnly);

                Thread.sleep(30000);
            }

            reflushStackAndLogs(stlogStatus, deleteOnly);
            lg.info(String.format("%s -- Stack %s had been removed, exit tracker thread now.", se, stacks.getStackname()));

        } catch (Exception e) {
            lg.error(String.format(String.format("%s -- Cloudformation Tracker %s failed: %s", se, stacks.getStackname(), e.getMessage())));
        }

        return this;
    }

    private String reflushStackAndLogs(String status, boolean check4delete) {
        DescribeStacksRequest descReq = new DescribeStacksRequest();
        descReq.setStackName(stacks.getStackname());
        for (Stack st : cfBuilder.describeStacks(descReq).getStacks()) {
            String statusnew = st.getStackStatus();
            if (!StringUtil.equals(status, statusnew)) {
                status = statusnew;
                //Updating new stack status into DB.
                StackLogs stl = new StackLogs();
                stl.setStackid(stacks.getStackid());
                stl.setComments(st.getStackStatusReason());
                stl.setStatus(statusnew);
                stl.setUsername(stacks.getUsername());
                stl.setCreatedat(new Date(System.currentTimeMillis()));
                stl.setUpdatedat(new Date(System.currentTimeMillis()));
                stlRepository.save(stl);

                stacks.setStatus(st.getStackStatus());
                List<Output> ots = st.getOutputs();
                JsonArray jsonArray = new JsonArray();
                for (Output ot : ots) {
                    JsonObject json = new JsonObject();
                    json.put("Key", ot.getOutputValue());
                    json.put("Value", ot.getOutputValue());
                    json.put("Desc", ot.getDescription());
                    jsonArray.add(json);
                }
                stacks.setStackoutputs(jsonArray.toString());
                lg.info(String.format("%s -- New stacklog saved: %s", se, stl));
            }
        }

        DescribeStackResourcesRequest descResReq = new DescribeStackResourcesRequest();
        descResReq.setStackName(stacks.getStackname());
        JsonArray jsonarray = new JsonArray();
        for (StackResource sr: cfBuilder.describeStackResources(descResReq).getStackResources()) {
            JsonObject json = new JsonObject();
            json.put("ResourceType", sr.getResourceType());
            json.put("ResourceStatus", sr.getResourceStatus());
            json.put("  n", sr.getPhysicalResourceId());
            json.put("LogicalResourceId", sr.getLogicalResourceId());
            json.put("TimeStamp", DICT.DF_YMDHMS.format(sr.getTimestamp()));
            json.put("Description", sr.getDescription());
            jsonarray.add(json);
        }

        DescribeStackEventsRequest descEvtReq = new DescribeStackEventsRequest();
        descEvtReq.setStackName(stacks.getStackname());
        JsonArray jsonarrayEvt = new JsonArray();
        for (StackEvent se : cfBuilder.describeStackEvents(descEvtReq).getStackEvents()) {
            JsonObject json = new JsonObject();
            json.put("EventID", se.getEventId());
            json.put("ResourceType", se.getResourceType());
            json.put("ResourceStatus", se.getResourceStatus());
            json.put("PhysicalResourceId", se.getPhysicalResourceId());
            json.put("LogicalResourceId", se.getLogicalResourceId());
            json.put("ResourceStatusReason", se.getResourceStatusReason());
            json.put("TimeStamp", DICT.DF_YMDHMS.format(se.getTimestamp()));
            jsonarrayEvt.add(json);
        }

        lg.info(String.format("%s -- Update stack resource as well: %s, %s", se, stacks.getStackid(), stacks.getStackname()));
        stacks.setStackresources(jsonarray.toString());
        stacks.setStackevents(jsonarrayEvt.toString());
        stacks.setUpdatedat(new Date(System.currentTimeMillis()));

        stRepository.save(stacks);
        return status;
    }


}
