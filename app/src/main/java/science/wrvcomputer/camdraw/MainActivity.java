package science.wrvcomputer.camdraw;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    ImageView img;
    String mCurrentPhotoPath;
    String mCurrentPhotoFilePath;
    Canvas mCanvas;
    Paint mPaint;
    Bitmap bmp, tempBitmap;
    Button resetButton;
    Button saveButton;
    boolean drawn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img  = (ImageView) findViewById(R.id.imageView);
        resetButton = (Button) findViewById(R.id.button2);
        saveButton = (Button) findViewById(R.id.button3);

        drawn = false;
        mPaint = new Paint();
        mPaint.setColor(0xff0000ff);
        mPaint.setStrokeWidth(15);


        img.setOnTouchListener(new View.OnTouchListener() {
            float startX, startY, curX, curY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    //mCanvas.drawRoundRect(new RectF(0,0,800,200), 2, 2, mPaint);
                    //setContentView(v);
                    if(!drawn){
                        mCanvas.drawBitmap(bmp, 0, 0, null);
                        drawn = true;
                    }
                    float[] coords = getPointerCoords(img, event);
                    startX = coords[0];
                    startY = coords[1];
                    mCanvas.drawCircle(startX,startY,15,mPaint);

                    img.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                    //event.getX(), event.getY();
                }
                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    //setContentView(v);
                    //int x = (int) event.getRawX();
                    //int y = (int) event.getRawY();
                    if(!drawn) {
                        mCanvas.drawBitmap(bmp, 0, 0, null);
                        drawn = true;
                    }

                    float[] coords = getPointerCoords(img, event);
                    curX = coords[0];
                    curY = coords[1];
                    mCanvas.drawLine(startX, startY, curX, curY, mPaint);

                    startX = curX;
                    startY = curY;

                    img.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mCurrentPhotoFilePath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.pick_color)
                    .setItems(R.array.colors, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch(which){
                                case 0:
                                    mPaint.setColor(Color.RED);
                                    break;
                                case 1:
                                    mPaint.setColor(Color.GREEN);
                                    break;
                                case 2:
                                    mPaint.setColor(Color.BLUE);
                                    break;
                                case 3:
                                    mPaint.setColor(Color.BLACK);
                                    break;
                                case 4:
                                    mPaint.setColor(Color.WHITE);
                                    break;
                                default:
                                    mPaint.setColor(Color.BLUE);
                                    break;
                            }
                        }
                    });
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void resetImage(View view){
        mCanvas.drawBitmap(bmp, 0, 0, null);
        drawn = true;
        img.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }

    public void saveImage(View view){
        View content = findViewById(R.id.imageView);
        content.setDrawingCacheEnabled(true);
        Bitmap bitmap = content.getDrawingCache();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        try
        {
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = File.createTempFile(imageFileName,".jpg", storageDir);
            //file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
            Toast.makeText(getApplicationContext(), "Picture saved in: " + file.getPath(), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //img.setImageURI(Uri.parse(mCurrentPhotoPath));
            bmp = BitmapFactory.decodeFile(mCurrentPhotoPath);
            bmp = imageOrientationValidator(bmp, mCurrentPhotoPath);
            img.setVisibility(View.VISIBLE);
            resetButton.setEnabled(true);
            saveButton.setEnabled(true);
            img.setImageBitmap(bmp);

            tempBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.RGB_565);
            mCanvas = new Canvas(tempBitmap);
        }
    }

    /*
     * Helper functions
     */
    private Bitmap imageOrientationValidator(Bitmap bitmap, String path) {

        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private Bitmap rotateImage(Bitmap source, float angle) {

        Bitmap bitmap = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                    matrix, true);
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
        }
        return bitmap;
    }

    final float[] getPointerCoords(ImageView view, MotionEvent e)
    {
        final int index = e.getActionIndex();
        final float[] coords = new float[] { e.getX(index), e.getY(index) };
        Matrix matrix = new Matrix();
        view.getImageMatrix().invert(matrix);
        matrix.postTranslate(view.getScrollX(), view.getScrollY());
        matrix.mapPoints(coords);
        return coords;
    }
}
