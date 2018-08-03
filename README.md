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
###### Create stack: 
curl -X PUT -d 'parametsjson={"KeyName": "eason_personal_rsa"}'  http://localhost:8080/beaver/createstack/eason/e3/us-west-2/ec2_simple_demo
###### Get stack info:
http://localhost:8080/beaver/stacks/e3 <br>
###### Get stack log info:
http://localhost:8080/beaver/stacklogs/e3 <br>
###### Delete stack:
curl -X DELETE http://localhost:8080/beaver/deletestack/e3 <br>
###### Show templates:
http://localhost:8080/beaver/templates <br>

##### Start service
export PROJECT_ROOT=/mnt/apps/modules/beaver && /mnt/apps/tools/jdk1.8.0_101/bin/java -jar target/gs-rest-rds-eason-0.1.0.jar 2>&1 >/dev/null &
