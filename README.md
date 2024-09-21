# Spring After Effects Backend API

Endpoint for batch video rendering for dynamic after effects layers. 

Run with localhost:8080/create , form-data with 2 images. (Will be extended to handle more than 2 images later).
'template' = template name (string)
'image1' = File (jpg/png)
'image2' = File (jpg/png)

Supports images to gif, images to video, videos to video, videos to gif. (Video->Gif converted and compressed with FFMPEG)

To create a new template (.aep), add it to the ae/ directory.
