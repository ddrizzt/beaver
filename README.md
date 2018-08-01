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
