package waldo.photos.exif.waldophotos;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
//import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String UrlSeperator = "/",
            Newline = "\n";

    EditText NetworkStoreUrl;
    TextView NetworkStoreList;
    TableLayout ImageList;

    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    protected ArrayList<String> GetXml(String fromUrl) {
        ArrayList<String> huh;
        ArrayList<String> items = new ArrayList<>();

        try {
            URL url = new URL(fromUrl);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser parser = factory.newPullParser();

            try {
                parser.setInput(getInputStream(url), "UTF_8");
            } catch (Exception exception) {
                return items;
            }

            boolean insideItem = false;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equalsIgnoreCase("contents")) {
                        insideItem = true;
                    } else if (parser.getName().equalsIgnoreCase("key") && insideItem) {
                        items.add(parser.nextText());
                    }
                } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("contents")) {
                    insideItem = false;
                }

                eventType = parser.next();
            }
        } catch (MalformedURLException e) {
            NetworkStoreList.setText(R.string.error_invalid_url);
        } catch (XmlPullParserException e) {
            NetworkStoreList.setText(R.string.error_io_exception);
        } catch (IOException e) {
            NetworkStoreList.setText(R.string.error_io_exception);
        }

        return items;
    }

    void ShowExifInfo(String baseUrl, ArrayList<String> photos) {
        NetworkStoreList.setVisibility(View.GONE);
        ImageList.setVisibility(View.VISIBLE);

        for (String photo : photos) {
            TableRow tableRow = (TableRow) LayoutInflater.from(this).inflate(R.layout.image_row, null);
            Bitmap bitmap = null;
            ExifInterface exif = null;
            int bitmapWidth = 0,
                    bitmapHeight = 0;
            double thumbnailRatio = 0;
            float[] latitudeLongitude = new float[] {};

            try {
                URL url = new URL(baseUrl + UrlSeperator + photo);
                InputStream inputStream = new BufferedInputStream(url.openStream());

                bitmap = BitmapFactory.decodeStream(inputStream);
                bitmapWidth = bitmap.getWidth();
                bitmapHeight = bitmap.getHeight();
                thumbnailRatio = 1.0 / (double) Math.max(bitmapWidth, bitmapHeight);

                exif = new ExifInterface(inputStream);
            } catch (Exception exception) {
            }

            StringBuilder exifData = new StringBuilder();
            if (bitmap != null) {
                /***********************************************************************************
                 retrieve desired exif data; add attributes as needed...
                 ***********************************************************************************/

                exifData.append("File Name: ");
                exifData.append(photo);
                exifData.append(Newline);
            } else {
                exifData.append(getResources().getString(R.string.no_exif_data));
            }

            /***********************************************************************************
             create a thumbnail for the image...
             this is quite memory-intensive so commenting out for the emulator;
             however, for a tablet or pc with adequate memory, the thumbnail can be set
             ***********************************************************************************/
            //bitmap.setWidth((int) Math.floor(bitmapWidth * thumbnailRatio));
            //bitmap.setHeight((int) Math.floor(bitmapHeight * thumbnailRatio));
            //((ImageView) tableRow.findViewById(R.id.image_row_thumbnail)).setImageBitmap(bitmap);

            ((TextView) tableRow.findViewById(R.id.image_row_exif_data)).setText(exifData.toString());

            ImageList.addView(tableRow);
        }
    }

    public void GetPhotos(View view) {
        switch (view.getId()) {
            case R.id.network_store_url_control:
                NetworkStoreList.setVisibility(View.VISIBLE);
                ImageList.setVisibility(View.GONE);

                String url = NetworkStoreUrl.getText().toString().trim();

                if (url.length() == 0 || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                    NetworkStoreList.setText(R.string.error_invalid_url);
                } else {
                    ArrayList<String> photos = GetXml(url);
                    if (photos.size() > 0) {
                        ShowExifInfo(url, photos);
                    } else {
                        NetworkStoreList.setText(R.string.error_no_data);
                    }
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NetworkStoreUrl = (EditText) findViewById(R.id.network_store_url);
        NetworkStoreList = (TextView) findViewById(R.id.network_store_list);
        ImageList = (TableLayout) findViewById(R.id.image_list);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
