package es.alejandrtf.alarmavispa.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.alejandrtf.alarmavispa.R;

public class Images {
    private static final String TAG = "ImagesUtilities";


    /**
     * Method that creates the file descriptor where the photo will be saved
     *
     * @return the file descriptor
     */
    public static File createOutputPictureFile(Context context) {
        //get the directory where I will save the photo: DevidePhotoDirectory/PhotosAlarmAvispa
        File picturesDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(R.string.appPhotosDirectory));
        if (!picturesDirectory.exists()) {
            //create it
            if (!picturesDirectory.mkdirs())
                return null;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(picturesDirectory.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        return file;
    }


    /**
     * Method reduces the size of a bitmap to maxWidth and maxHeight
     *
     * @param context   Context
     * @param fileUri   uri of the file containing the image
     * @param maxWidth
     * @param maxHeight
     * @return the reduced bitmap
     */
    public static Bitmap reduceBitmap(Context context, Uri fileUri, int maxWidth, int maxHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(fileUri), null, options);
            int currentHeight = options.outHeight;
            int currentWidth = options.outWidth;
            options.inSampleSize = (int) Math.max(Math.ceil((currentWidth / maxWidth)), Math.ceil((currentHeight / maxHeight)));
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(fileUri), null, options);
        } catch (FileNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.error_loading_photo), Toast.LENGTH_SHORT).show();
            Log.e(TAG, context.getString(R.string.error_loading_photo), e.getCause());
            return null;
        }
    }


    /**
     * Method return a BitmapDescriptor mades up of a drawable and a background drawable (will be the shape with color of the marker) that
     * can be use as marker in Google Maps v2
     *
     * @param context
     * @param drawableResourdeId id of the drawable you want put inside marker
     * @param ic_marker        the marker you want to use
     * @param tintColor        color of the background marker
     * @return
     */
    public static BitmapDescriptor getBitmapFromVector(@NonNull Context context,
                                                       @DrawableRes int drawableResourdeId,
                                                       @DrawableRes int ic_marker, @ColorInt int tintColor) {
        //BACKGROUND
        Drawable background = ContextCompat.getDrawable(context, ic_marker);
        if (background == null) {
            Log.e(TAG, "Request vector resource was not found: background");
            return BitmapDescriptorFactory.defaultMarker();
        }
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(),
                background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(background, tintColor);//paint it with color tintColor

        //IMAGE INSIDE MARKER
        Drawable drawable = ResourcesCompat.getDrawable(
                context.getResources(), drawableResourdeId, null);
        if (drawable == null) {
            Log.e(TAG, "Requested vector resource was not found");
            return BitmapDescriptorFactory.defaultMarker();
        }

        //drawable.setBounds(20,10,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        //I must center "drawable" in canvas
        // To get it I calculte left X coordinate of my drawable inside the canvas and
        // the left Y coordinate of my drawable inside the canvas too.
        int xCoordinateDrawable = (canvas.getWidth() - drawable.getIntrinsicWidth()) / 2;
        int yCoordinateDrawable = (canvas.getHeight() - drawable.getIntrinsicHeight()) / 2;

        drawable.setBounds(xCoordinateDrawable+(int)context.getResources().getDimension(R.dimen.displacement_x),
                yCoordinateDrawable+5,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        //DRAW ALL
        background.draw(canvas);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }



}
