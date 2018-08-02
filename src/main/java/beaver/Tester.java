package beaver;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;
import jodd.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Tester {
    public static void main(String[] args) throws IOException {

        String templateBody = FileUtil.readString(new File("/Users/drizzt/PycharmProjects/post_dovecot_demo/cf_template.json"));
        AmazonCloudFormation stackbuilder = new AmazonCloudFormationClient(new PropertiesFileCredentialsProvider("/Users/drizzt/Documents/workspace/beaver/src/main/resources/AwsCredentials.properties"));
        stackbuilder.setRegion(Region.getRegion(Regions.fromName("us-west-2")));


//        CreateStackRequest streq = new CreateStackRequest();
//        streq.setStackName("e1");
//        streq.setTemplateBody(templateBody);
//        streq.setCapabilities(Arrays.asList("CAPABILITY_IAM"));
//        Parameter p1 = new Parameter();
//        p1.setParameterKey("KeyName");
//        p1.setParameterValue("eason_personal_rsa");
//        streq.setParameters(Arrays.asList(p1));
//
//        stackbuilder.createStack(streq);
//        System.out.println("Creating a stack called " + streq.getStackName() + ".");
//        CreateStackResult rs = stackbuilder.createStack(streq);
//        System.out.println(rs.getStackId());

//        ListStacksRequest lsr = new ListStacksRequest();
//        lsr.setStackStatusFilters(Arrays.asList("DELETE_COMPLETE"));
//        ListStacksResult resp = stackbuilder.listStacks(lsr);
//        List<StackSummary> summarys = resp.getStackSummaries();
//        for (StackSummary summary : summarys) {
//            System.out.println(String.format("%s, %s, %s", summary.getStackId(), summary.getStackName(), summary.getStackStatus(), summary.getStackStatusReason()));
//        }

        DescribeStacksRequest req = new DescribeStacksRequest();
        req.setStackName("e1");

        try {
            DescribeStacksResult res = stackbuilder.describeStacks(req);
            List<Stack> ress = res.getStacks();
            for (Stack stack : ress) {
                System.out.println(stack);
            }
        } catch(AmazonCloudFormationException e) {
            e.printStackTrace();
            System.out.println(String.format("%s, %s, %s, %s, %s", e.getStatusCode(), e.getMessage(), e.getErrorCode(), e.getCause(), e.getErrorType()));
        }

    }
}

