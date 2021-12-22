# Removing Tag Jobs Stuck in 'In Progress'

Occasionally, tagging jobs get stuck in 'In Progress'.

<kbd>![Screen Shot 2021-12-22 at 3 05 50 pm](https://user-images.githubusercontent.com/34686302/147068980-b1e31dae-f5b4-4eb5-b2bf-6f4409b51f5e.png)</kbd>

We can manually remove these jobs from the DynamoDB table in which they live - usually `tag-manager-background-jobs-PROD`.

Stuck jobs will appear here with the job status `waiting`, and can be deleted from the table.
