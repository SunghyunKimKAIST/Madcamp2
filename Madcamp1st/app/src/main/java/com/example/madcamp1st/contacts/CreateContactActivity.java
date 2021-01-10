package com.example.madcamp1st.contacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.madcamp1st.R;

public class CreateContactActivity extends Activity {
    Editable name, number;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);

        name = ((EditText)findViewById(R.id.name_create_contact)).getText();
        number = ((EditText)findViewById(R.id.number_create_contact)).getText();
    }

    public void sendContact(View view){
        if(name.length() == 0) {
            Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if(number.length() == 0) {
            Toast.makeText(this, "번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("name", name.toString());
        intent.putExtra("number", number.toString());
        setResult(RESULT_OK, intent);

        finish();
    }
}