# Removing Tag Jobs Stuck in 'In Progress'

Occasionally, tagging jobs get stuck in 'In Progress'.

We can manually remove these jobs from the DynamoDB table in which they live - usually `tag-manager-background-jobs-PROD`.

Stuck jobs will appear here with the job status `waiting`, and can be deleted from the table.