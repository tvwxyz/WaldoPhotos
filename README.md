# WaldoPhotos

I originally wanted to create an Android app for this excercise. However, after a few hours of work I encountered some technical issues, namely the emulator's limited memory when dealing with large image files. After careful consideration, an Android app probably isn't the best platform but I had already written lots of code.

The Java app keeps getting the an "unsupported image format file" error for every image. I'm not sure if this is an Android issue or a file issue but resolving that would provide the correct EXIF data.

Anyway, my thinking was basically this (using Java as the language of choice)...

1. Grab the XML file with the image reference data.
2. For each image, pull that image data (along with its encoded EXIF data).
3. Create a data structure to hold the EXIF data; in Java, I would use a nested Map/ArrayList as such: Map&lt;String, Map&lt;String, String&gt;&gt; 
  This would allow a data structure ordered by unique image filenames then by unique EXIF attributes and their values.
4. Finally, send that data structure to the server for storage or querying (via SQL, Linq, etc).
