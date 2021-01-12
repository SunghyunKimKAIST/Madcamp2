package com.example.madcamp1st.images;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madcamp1st.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.internal.EverythingIsNonNull;

public class Fragment_Images extends Fragment {
    private View mView;
    private ImageAdapter mAdapter;

    private List<Image> internalImageFilepaths;
    private final String ORIGINAL_DIRECTORY_NAME = "original";
    private final String THUMBNAIL_DIRECTORY_NAME = "thumbnail";

    private final String DB_URL = "http://192.249.18.163:1234/";
    private ImageService imageService;

    private final int REQUEST_GET_IMAGE = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mView = inflater.inflate(R.layout.fragment_images, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        internalImageFilepaths = loadInternalImageFilepaths();

        // recyclerview
        RecyclerView recyclerView = mView.findViewById(R.id.recyclerView_images);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        mAdapter = new ImageAdapter(internalImageFilepaths);
        recyclerView.setAdapter(mAdapter);

        // DB 통신
        imageService = new Retrofit.Builder()
                .baseUrl(DB_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ImageService.class);

        Call<List<Image>> call = imageService.getAllImageName();

        call.enqueue(new Callback<List<Image>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<Image>> call, Response<List<Image>> response) {
                if(response.isSuccessful())
                    syncImages(internalImageFilepaths, response.body());
                else
                    Toast.makeText(getContext(), "getAllImageName: DB에서 이미지 리스트를 불러오는데 실패했습니다\nHTTP status code: " + response.code(), Toast.LENGTH_LONG).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<Image>> call, Throwable t) {
                Toast.makeText(getContext(), "getAllImageName: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();
            }
        });

        mView.findViewById(R.id.button_get_image).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Get Album"), REQUEST_GET_IMAGE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_GET_IMAGE && resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();
            String name = getFileNameFromUri(uri);

            File original = new File(new File(getContext().getFilesDir(), ORIGINAL_DIRECTORY_NAME), name);
            File thumbnail = new File(new File(getContext().getFilesDir(), THUMBNAIL_DIRECTORY_NAME), name);

            try(InputStream originalIS = getContext().getContentResolver().openInputStream(uri)){
                Files.copy(originalIS, original.toPath(), StandardCopyOption.REPLACE_EXISTING);

                if(!thumbnail.exists())
                    thumbnail.createNewFile();

                try(FileOutputStream thumbnailFOS = new FileOutputStream(thumbnail)){
                    getContext().getContentResolver().loadThumbnail(uri, new Size(360, 360), null).compress(Bitmap.CompressFormat.PNG, 100, thumbnailFOS);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "get image from internal gallery error: IOException", Toast.LENGTH_SHORT).show();

                if(original.exists())
                    original.delete();

                if(thumbnail.exists())
                    thumbnail.delete();

                return;
            }

            internalImageFilepaths.add(new Image(name, original, thumbnail));
            internalImageFilepaths.sort((l, r) -> l.name.compareTo(r.name));
            mAdapter.updateImages(internalImageFilepaths);
        }
    }

    // 이거 꼭 써야함? 모르겠음. 일단 잘 굴러가니까 씀
    // https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content
    private String getFileNameFromUri(Uri uri) {
        String result = null;

        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }

        if (result == null) {
            result = uri.getPath();

            int cut = result.lastIndexOf('/');

            if (cut != -1)
                result = result.substring(cut + 1);
        }

        return result;
    }

    private List<Image> loadInternalImageFilepaths(){
        File originalDirectory = new File(getContext().getFilesDir(), ORIGINAL_DIRECTORY_NAME);
        File thumbnailDirectory = new File(getContext().getFilesDir(), THUMBNAIL_DIRECTORY_NAME);

        if(!originalDirectory.exists())
            originalDirectory.mkdir();

        if(!thumbnailDirectory.exists())
            thumbnailDirectory.mkdir();

        File[] originalFiles = originalDirectory.listFiles();
        File[] thumbnailFiles = thumbnailDirectory.listFiles();

        if(originalFiles == null || thumbnailFiles == null) {
            Toast.makeText(getContext(), "loadInternalImageFilepaths error", Toast.LENGTH_LONG).show();
            return new ArrayList<>();
        }

        if(originalFiles.length != thumbnailFiles.length) {
            Toast.makeText(getContext(), "original file number does not equals with thumbnail file number", Toast.LENGTH_LONG).show();
            return new ArrayList<>();
        }

        List<Image> res = new ArrayList<>();

        for(int i = 0; i < originalFiles.length; i++)
            res.add(new Image(originalFiles[i].getName(), originalFiles[i], thumbnailFiles[i]));

        res.sort((l, r) -> l.name.compareTo(r.name));

        return res;
    }

    // 동시성 문제 존재
    private void getDBImageAndStoreAsync(String imageFilepath){
        imageService.getImage(imageFilepath).enqueue(new Callback<ResponseBody>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    File original = new File(new File(getContext().getFilesDir(), ORIGINAL_DIRECTORY_NAME), imageFilepath);
                    File thumbnail = new File(new File(getContext().getFilesDir(), THUMBNAIL_DIRECTORY_NAME), imageFilepath);

                    try {
                        try(InputStream responseIS = response.body().byteStream()) {
                            Files.copy(responseIS, original.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }

                        if(!thumbnail.exists())
                            thumbnail.createNewFile();

                        try(OutputStream thumbnailOS = new FileOutputStream(thumbnail)) {
                            decodeThumbnailFromFile(original.getPath(), 360, 360).compress(Bitmap.CompressFormat.PNG, 100, thumbnailOS);
                        }

                        internalImageFilepaths.add(new Image(imageFilepath, original, thumbnail));
                        internalImageFilepaths.sort((l, r) -> l.name.compareTo(r.name));
                        mAdapter.updateImages(internalImageFilepaths);
                    }catch (IOException e){
                        e.printStackTrace();
                        Toast.makeText(getContext(), "getImage: DB에서 가져온 이미지를 저장하는데 실패했습니다", Toast.LENGTH_LONG).show();

                        if(original.exists())
                            original.delete();

                        if(thumbnail.exists())
                            thumbnail.delete();
                    }
                }
                else
                    Toast.makeText(getContext(), "getImage: DB에서 이미지를 가져오는데 실패했습니다\nHTTP status code: " + response.code(), Toast.LENGTH_LONG).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "getImage: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();
            }
        });
    }

    // copied from android document
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth)
                inSampleSize *= 2;
        }

        return inSampleSize;
    }

    private Bitmap decodeThumbnailFromFile(String pathName, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    private void dbCreateImage(File originalFile){
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(originalFile.toURI().toString()));
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), originalFile);

        MultipartBody.Part body = MultipartBody.Part.createFormData("image", originalFile.getName(), requestFile);

        RequestBody description = RequestBody.create(MultipartBody.FORM, "description");

        imageService.createImage(description, body).enqueue(new Callback<ResponseBody>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(!response.isSuccessful())
                    Toast.makeText(getContext(), "dbPostImage: DB에 이미지를 업로드하는데 실패했습니다\nHTTP status code: " + response.code(), Toast.LENGTH_LONG).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "dbPostImage: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();
            }
        });
    }

    private <T> T nextOrNull(Iterator<T> iterator){
        if(iterator.hasNext())
            return iterator.next();
        else
            return null;
    }

    private void syncImages(List<Image> internalImages, List<Image> dbImages){
        dbImages.sort((l, r) -> l.name.compareTo(r.name));

        Iterator<Image> internalIterator = internalImages.listIterator();
        Iterator<Image> dbIterator = dbImages.iterator();

        Image internalImage = nextOrNull(internalIterator);
        Image dbImage = nextOrNull(dbIterator);

        while(internalImage != null && dbImage != null){
            int compare = internalImage.name.compareTo(dbImage.name);

            if(compare == 0){
                internalImage = nextOrNull(internalIterator);
                dbImage = nextOrNull(dbIterator);
            } else if(compare < 0) {
                dbCreateImage(internalImage.original);

                internalImage = nextOrNull(internalIterator);
            } else{
                getDBImageAndStoreAsync(dbImage.name);

                dbImage = nextOrNull(dbIterator);
            }
        }

        while(internalImage != null){
            dbCreateImage(internalImage.original);

            internalImage = nextOrNull(internalIterator);
        }

        while(dbImage != null){
            getDBImageAndStoreAsync(dbImage.name);

            dbImage = nextOrNull(dbIterator);
        }
    }
}