package es.alejandrtf.alarmavispa.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.utilities.Images;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private List<String> photoUriList;
    private Context context;

    public PhotoAdapter(Context context, List<String> photoUriList) {
        this.photoUriList = photoUriList;
        this.context=context;
    }


    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recyclerview_photos, parent, false);
        PhotoViewHolder photoViewHolder=new PhotoViewHolder(itemView);
        return photoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String photUri=photoUriList.get(position);
        holder.bindPhotoUri(photUri,this,photoUriList);
    }

    @Override
    public int getItemCount() {
        return photoUriList.size();
    }

    public Context getContext(){
        return this.context;
    }




    /**
     * VIEWHOLDER
     */
    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private FloatingActionButton fabDeletePhoto;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto=itemView.findViewById(R.id.ivPhoto);
            fabDeletePhoto=itemView.findViewById(R.id.fab_delete_photo);
        }


        public void bindPhotoUri(String uri, final PhotoAdapter adapter, final List<String> photoUriList){
            fabDeletePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    photoUriList.remove(getAdapterPosition());
                    adapter.notifyDataSetChanged();
                }
            });
            loadPhoto(adapter.getContext(),uri,ivPhoto);
        }


        public void loadPhoto(Context context,String uri, ImageView ivPhoto){
            //ivPhoto.setImageURI(Uri.parse(uri));
            Bitmap reducedPhoto=Images.reduceBitmap(context,Uri.parse(uri),ivPhoto.getMaxWidth(),ivPhoto.getMaxHeight());
            if(reducedPhoto!=null)
                ivPhoto.setImageBitmap(reducedPhoto);
        }
    }
}
