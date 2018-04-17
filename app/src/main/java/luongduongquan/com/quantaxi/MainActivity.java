package luongduongquan.com.quantaxi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import luongduongquan.com.quantaxi.Model.User;
import luongduongquan.com.quantaxi.Utils.Common;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

	Button btnSignin, btnRegister;

	FirebaseAuth mAuth;
	FirebaseDatabase dataBase;
	DatabaseReference userReference;

	RelativeLayout rootLayout;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/cooper.ttf")
				.setFontAttrId(R.attr.fontPath)
				.build()
		);
		setContentView(R.layout.activity_main);

		//Init FireBase
		mAuth = FirebaseAuth.getInstance();
		dataBase = FirebaseDatabase.getInstance();
		userReference = dataBase.getReference(Common.USERS_TAG);

		// Init View
		btnSignin = findViewById(R.id.btnSignIn);
		btnRegister = findViewById(R.id.btnRegister);
		rootLayout = findViewById(R.id.rootLayout);

		btnSignin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showLoginDialog();
			}
		});

		btnRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showRegisterDialog();
			}
		});


	}

	private void showLoginDialog() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Login");
		dialog.setMessage("Please input email to login");

		LayoutInflater inflater = LayoutInflater.from(this);
		View login_layout = inflater.inflate(R.layout.login_layout, null);

		final MaterialEditText edtEmail 		= login_layout.findViewById(R.id.edtEmail_login);
		final MaterialEditText edtPassword 	= login_layout.findViewById(R.id.edtPassword_login);

		dialog.setView(login_layout);

		//set buttons
		dialog.setPositiveButton("Login", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				// Check validation
				if(TextUtils.isEmpty(edtEmail.getText().toString())){
					Snackbar.make(rootLayout, "Please enter email", Snackbar.LENGTH_SHORT).show();
					return;
				}
				if(TextUtils.isEmpty(edtPassword.getText().toString())){
					Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
					return;
				}

				mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
						.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
							@Override
							public void onSuccess(AuthResult authResult) {
								startActivity(new Intent(MainActivity.this, Welcome.class));
								finish();
							}
						})
						.addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								Snackbar.make(rootLayout, "Login Fail!", Snackbar.LENGTH_SHORT).show();
							}
						});

			}
		});

		dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dialog.show();

	}

	private void showRegisterDialog() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Register");
		dialog.setMessage("Please input email to register");

		LayoutInflater inflater = LayoutInflater.from(this);
		View register_layout = inflater.inflate(R.layout.register_layout, null);

		final MaterialEditText edtEmail 		= register_layout.findViewById(R.id.edtEmail_register);
		final MaterialEditText edtPassword 	= register_layout.findViewById(R.id.edtPassword_register);
		final MaterialEditText edtName 		= register_layout.findViewById(R.id.edtName_register);
		final MaterialEditText edtPhone 		= register_layout.findViewById(R.id.edtPhone_register);

		dialog.setView(register_layout);

		//set buttons
		dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				// Check validation
				if(TextUtils.isEmpty(edtEmail.getText().toString())){
					Snackbar.make(rootLayout, "Please enter email", Snackbar.LENGTH_SHORT).show();
					return;
				}
				if(TextUtils.isEmpty(edtPassword.getText().toString())){
					Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
					return;
				}
				if(TextUtils.isEmpty(edtName.getText().toString())){
					Snackbar.make(rootLayout, "Please enter name", Snackbar.LENGTH_SHORT).show();
					return;
				}
				if(TextUtils.isEmpty(edtPhone.getText().toString())){
					Snackbar.make(rootLayout, "Please enter phone", Snackbar.LENGTH_SHORT).show();
					return;
				}

				mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
						.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
							@Override
							public void onSuccess(AuthResult authResult) {
								// Save user to database

								User user = new User();
								user.setEmail(edtEmail.getText().toString());
								user.setPassword(edtPassword.getText().toString());
								user.setName(edtName.getText().toString());
								user.setPhone(edtPhone.getText().toString());

								// Use email to key in FireBase
								userReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
										.setValue(user)
										.addOnSuccessListener(new OnSuccessListener<Void>() {
											@Override
											public void onSuccess(Void aVoid) {
												Snackbar.make(rootLayout, "Register successfully!", Snackbar.LENGTH_SHORT).show();
											}
										})
										.addOnFailureListener(new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
											Snackbar.make(rootLayout, "Register Fail!", Snackbar.LENGTH_SHORT).show();
										}
								});
							}
						})
						.addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								Snackbar.make(rootLayout, "Register Fail!", Snackbar.LENGTH_SHORT).show();
							}
				});

			}
		});

		dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dialog.show();

	}
}
