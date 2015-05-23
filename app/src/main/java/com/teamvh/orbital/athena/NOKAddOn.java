//package com.teamvh.orbital.athena;
//
///**
// * Created by YANG on 5/22/2015.
// */
//
//        import android.content.Intent;
//        import android.support.v7.app.ActionBarActivity;
//        import android.os.Bundle;
//        import android.view.Menu;
//        import android.view.MenuItem;
//        import android.view.View;
//        import android.widget.Button;
//        import android.widget.EditText;
//        import android.widget.Toast;
//
//public class NOKAddOn extends ActionBarActivity implements android.view.View.OnClickListener{
//
//    Button btnSave ,  btnDelete;
//    Button btnClose;
//    EditText editTextName;
//    EditText editTextEmail;
//    EditText editTextAge;
//    private int _nok_id=0;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_nok_details);
//
//        btnSave = (Button) findViewById(R.id.btnSave);
//        btnDelete = (Button) findViewById(R.id.btnDelete);
//        btnClose = (Button) findViewById(R.id.btnClose);
//
//        editTextName = (EditText) findViewById(R.id.editTextName);
//        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
//        editTextAge = (EditText) findViewById(R.id.editTextAge);
//
//        btnSave.setOnClickListener(this);
//        btnDelete.setOnClickListener(this);
//        btnClose.setOnClickListener(this);
//
//
//        _nok_id =0;
//        Intent intent = getIntent();
//        _nok_id =intent.getIntExtra("nok_Id", 0);
//        SQLControlllerNOK repo = new SQLControlllerNOK(this);
//        NOKInfo noks = new NOKInfo();
//        noks = repo.getNOKByID(_nok_id);
//
//        editTextAge.setText(String.valueOf(noks.nok_name));
//        editTextName.setText(noks.nok_phone);
//        editTextEmail.setText(noks.nok_email);
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        return super.onOptionsItemSelected(item);
//    }
//
//    public void onClick(View view) {
//        if (view == findViewById(R.id.btnSave)){
//            SQLControlllerNOK repo = new SQLControlllerNOK(this);
//            NOKInfo nok = new NOKInfo();
//            nok.nok_name= editTextAge.getText().toString();
//            nok.nok_email=editTextEmail.getText().toString();
//            nok.nok_phone= Integer.parseInt(editTextName.getText().toString());
//            nok.nok_ID=_nok_id;
//
//            if (_nok_id==0){
//                _nok_id = repo.insert(nok);
//
//                Toast.makeText(this,"New Nok Insert",Toast.LENGTH_SHORT).show();
//            }else{
//                repo.update(nok);
//                Toast.makeText(this,"Nok Record updated",Toast.LENGTH_SHORT).show();
//            }
//        }else if (view== findViewById(R.id.btnDelete)){
//            SQLControlllerNOK repo = new SQLControlllerNOK(this);
//            repo.delete(_nok_id);
//            Toast.makeText(this, "Nok Record Deleted", Toast.LENGTH_SHORT);
//            finish();
//        }else if (view== findViewById(R.id.btnClose)){
//            finish();
//        }
//
//
//    }
//
//}