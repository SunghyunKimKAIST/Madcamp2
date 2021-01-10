package com.example.madcamp1st.images;

import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

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

    private List<File> internalImageFilepaths;
    private final String imagesDirectoryName = "images";

    private final String DB_URL = "http://192.249.18.163:1234/";
    private ImageService imageService;

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

        Call<List<ImageFileName>> call = imageService.getAllImageName();

        call.enqueue(new Callback<List<ImageFileName>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<ImageFileName>> call, Response<List<ImageFileName>> response) {
                if(response.isSuccessful()){
                    List<String> dbImageNames = response.body().stream().map(imageFileName -> imageFileName.name).collect(Collectors.toList());

                    syncImages(internalImageFilepaths, dbImageNames);
                } else
                    Toast.makeText(getContext(), "getAllImageName: DB에서 이미지 리스트를 불러오는데 실패했습니다\nHTTP status code: " + response.code(), Toast.LENGTH_LONG).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<ImageFileName>> call, Throwable t) {
                Toast.makeText(getContext(), "getAllImageName: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<File> loadInternalImageFilepaths(){
        File directory = new File(getContext().getFilesDir(), imagesDirectoryName);

        if(!directory.exists())
            directory.mkdir();

        File[] listFiles = directory.listFiles();

        if(listFiles == null) {
            Toast.makeText(getContext(), "loadInternalImageFilepaths error", Toast.LENGTH_LONG).show();
            return new ArrayList<>();
        }

        List<File> res = new ArrayList<>(Arrays.asList(listFiles));
        res.sort((l, r) -> l.getName().compareTo(r.getName()));

        return res;
    }

    // 동시성 문제 존재
    private void getDBImageAndStoreAsync(String imageFilepath){
        imageService.getImage(imageFilepath).enqueue(new Callback<ResponseBody>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    File directory = new File(getContext().getFilesDir(), imagesDirectoryName);
                    File file = new File(directory, imageFilepath);

                    try {
                        if (!file.exists())
                            file.createNewFile();

                        try(FileOutputStream fos = new FileOutputStream(file)){
                            fos.write(response.body().bytes());

                            internalImageFilepaths.add(file);
                            internalImageFilepaths.sort((l, r) -> l.getName().compareTo(r.getName()));
                            mAdapter.updateImages(internalImageFilepaths);
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                        Toast.makeText(getContext(), "getImage: DB에서 가져온 이미지를 저장하는데 실패했습니다", Toast.LENGTH_LONG).show();

                        if(file.exists())
                            file.delete();
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

    private void dbPostImage(File internalImageFilepath){
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(internalImageFilepath.toURI().toString()));
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), internalImageFilepath);

        MultipartBody.Part body = MultipartBody.Part.createFormData("image", internalImageFilepath.getName(), requestFile);

        RequestBody description = RequestBody.create(MultipartBody.FORM, "description");

        imageService.postImage(description, body).enqueue(new Callback<ResponseBody>() {
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

    private void syncImages(List<File> internalImageFilepaths, List<String> dbImageNames){
        dbImageNames.sort(null);

        ListIterator<File> internalIterator = internalImageFilepaths.listIterator();
        Iterator<String> dbIterator = dbImageNames.iterator();

        File internalImageFilepath = nextOrNull(internalIterator);
        String dbImageName = nextOrNull(dbIterator);

        while(internalImageFilepath != null && dbImageName != null){
            int compare = internalImageFilepath.getName().compareTo(dbImageName);

            if(compare == 0){
                internalImageFilepath = nextOrNull(internalIterator);
                dbImageName = nextOrNull(dbIterator);
            } else if(compare < 0) {
                dbPostImage(internalImageFilepath);

                internalImageFilepath = nextOrNull(internalIterator);
            } else{
                getDBImageAndStoreAsync(dbImageName);

                dbImageName = nextOrNull(dbIterator);
            }
        }

        while(internalImageFilepath != null){
            dbPostImage(internalImageFilepath);

            internalImageFilepath = nextOrNull(internalIterator);
        }

        while(dbImageName != null){
            getDBImageAndStoreAsync(dbImageName);

            dbImageName = nextOrNull(dbIterator);
        }
    }
}