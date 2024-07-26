# aeapi

Endpoint for batch video rendering for dynamic after effects layers, to be used with Holo AI's mobile app.

Run with localhost:8080/create , form-data with 2 images.
'image1' = File (jpg/png)
'image2' = File (jpg/png)

Supports images to gif, images to video, videos to video, videos to gif.

To create a new template (.aep), add it to the ae/ directory.