# Beaver  API
Beaver is a restful API to initial AWS resource by pre-defined AWS Cloudformation templates.
It will call AWS cloudformation to create related AWS resouces by Cloudformation template.

#
Functions:
- Users can submit "creating stacks" request to beaver.
- Users can real-time trace the stacks status.
- Users can delete the stacks once it is no need.
- More function TBD: Like integration with CloudWatch, monitoring each stack lifetime and give estimation daily cost.

Know limitations:
- When user send delete stack request. API can't return whole deleting information on each resources. 
API now only can mark the stacks status to DELETE_COMPLETE once the deleting is complete. (Since AWS API dosent supply these information once the deleting is completed.)

##### Sample URL:
http://35.162.250.81:8080/beaver/createstack?stackname=simple6&templatename=ec2_simple_demo&user=eason&availableregion=us-west-2&parametsjson=%7b%22KeyName%22%3a%22eason_personal_rsa%22%7d <br>
http://35.162.250.81:8080/beaver/stacks?stackname=simple6 <br>
http://35.162.250.81:8080/beaver/stacklogs?stackname=simple6 <br>

##### Start service
export PROJECT_ROOT=/mnt/apps/modules/beaver-1.0 && /mnt/apps/tools/jdk1.8.0_101/bin/java -jar target/gs-rest-rds-eason-0.1.0.jar 2>&1 >/dev/null &
