package luongduongquan.com.quantaxi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import luongduongquan.com.quantaxi.Utils.Common;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

	TextView tvLogin_Register, tvDontHave_Register;
	EditText edtEmail_Register;
	EditText edtPassword_Register;
	Button btnLogin_Register;
	Button btnRegister_Register;

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener fireBaseAuthListener;
	ProgressDialog loadingBar;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_register);

		mAuth = FirebaseAuth.getInstance();
//		fireBaseAuthListener = new FirebaseAuth.AuthStateListener(){
//
//			@Override
//			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//				FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//				if(user != null){
//					Intent intent = new Intent(LoginActivity.this, MapActivity.class);
//					startActivity(intent);
//					finish();
//					return;
//				}
//			}
//		};

		initView();
		loadingBar = new ProgressDialog(this);

		String dataFromIntent = getIntent().getExtras().getString(Common.LOGIN_INTENT, "Error");
		if(dataFromIntent.isEmpty() || dataFromIntent.equals("Error")){
			Toast.makeText(LoginActivity.this, "Error...", Toast.LENGTH_SHORT).show();
		} else {
			if(dataFromIntent.equals(Common.CUSTOMER_LOGIN_TITLE)){
				setLoginScreen(Common.CUSTOMER_LOGIN_TITLE);
			} else if(dataFromIntent.equals(Common.DRIVER_LOGIN_TITLE)){
				setLoginScreen(Common.DRIVER_LOGIN_TITLE);
			}
		}

	}

	private void initView(){
		tvLogin_Register = findViewById(R.id.tvDriverLogin_register);
		tvDontHave_Register = findViewById(R.id.tvDontHaveAccount_register);
		edtEmail_Register = findViewById(R.id.edtEmail_register);
		edtPassword_Register = findViewById(R.id.edtPassword_register);
		btnLogin_Register = findViewById(R.id.btnLogin_register);
		btnRegister_Register = findViewById(R.id.btnRegister_register);

		btnLogin_Register.setOnClickListener(this);
		tvDontHave_Register.setOnClickListener(this);
		btnRegister_Register.setOnClickListener(this);
	}

	private void setLoginScreen(String title){
		tvLogin_Register.setText(title);
		btnRegister_Register.setVisibility(View.GONE);
	}

	private void setRegisterScreen(){
		if(tvLogin_Register.getText().toString().equals(Common.CUSTOMER_LOGIN_TITLE)){
			tvLogin_Register.setText("Customer Register");
		} else {
			tvLogin_Register.setText("Driver Register");
		}
		btnRegister_Register.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		String userName = edtEmail_Register.getText().toString();
		String password = edtPassword_Register.getText().toString();
		switch (v.getId()){
			case R.id.btnLogin_register :
				loadingBar.setTitle("Logging");
				loadingBar.setMessage("Please wait...");
				loadingBar.show();
				loginAccountToFireBase(userName, password);
				break;
			case R.id.tvDontHaveAccount_register:
				v.post(new Runnable() {
					@Override
					public void run() {
						btnLogin_Register.setVisibility(View.GONE);
						tvDontHave_Register.setVisibility(View.GONE);
						setRegisterScreen();
					}
				});

				break;
			case R.id.btnRegister_register:

				loadingBar.setTitle("Registering");
				loadingBar.setMessage("Please wait...");
				loadingBar.show();

				registerAccountToFireBase(userName, password);
				break;
		}
	}

	private void registerAccountToFireBase(String email, String password){
		if(email.isEmpty() || password.isEmpty()){
			Toast.makeText(LoginActivity.this, "Please input email or password.", Toast.LENGTH_SHORT).show();
		} else {
			mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					if(task.isSuccessful()){
						// Xử lý sau khi đăng ký thành công

						String user_id = mAuth.getCurrentUser().getUid();
						DatabaseReference userDataReference = FirebaseDatabase.getInstance().getReference().child(Common.USERS_TAG);
						Intent intentMain;
						if(tvLogin_Register.getText().equals("Customer Register")){
							intentMain = new Intent(LoginActivity.this, CustomerMapActivity.class);
							userDataReference.child(Common.CUSTOMER_TAG).child(user_id).setValue(true);
						} else {
							intentMain = new Intent(LoginActivity.this, DriverMapActivity.class);
							userDataReference.child(Common.DRIVER_TAG).child(user_id).setValue(true);
						}
						loadingBar.dismiss();
						intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
						startActivity(intentMain);
						finish();


					} else {
						// Xử lý khi Fail.
						Toast.makeText(LoginActivity.this, "Error, try again", Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
	}

	private void loginAccountToFireBase (String email, String password){
		if(email.isEmpty() || password.isEmpty()){
			Toast.makeText(LoginActivity.this, "Please input email or password.", Toast.LENGTH_SHORT).show();
		} else {
			mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					if(task.isSuccessful()){
						Toast.makeText(LoginActivity.this, "Login SUCCESS!", Toast.LENGTH_SHORT).show();
						// Xử lý sau khi đăng ký thành công
//						Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
//						intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//						startActivity(intentMain);
//						finish();

//						String user_id = mAuth.getCurrentUser().getUid();
//						DatabaseReference userDataReference = FirebaseDatabase.getInstance().getReference().child(Common.USERS_TAG).child(user_id);
//						userDataReference.setValue(true);


						Intent intentMain;
						if(tvLogin_Register.getText().equals(Common.CUSTOMER_LOGIN_TITLE)){
							intentMain = new Intent(LoginActivity.this, CustomerMapActivity.class);
						} else {
							intentMain = new Intent(LoginActivity.this, DriverMapActivity.class);
						}
						loadingBar.dismiss();

						intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
						startActivity(intentMain);
						finish();
					} else {
						// Xử lý khi Fail.
						Toast.makeText(LoginActivity.this, "Login fail!", Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
//		mAuth.addAuthStateListener(fireBaseAuthListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
//		mAuth.addAuthStateListener(fireBaseAuthListener);
	}
}
