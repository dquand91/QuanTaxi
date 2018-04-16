package luongduongquan.com.quantaxi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import luongduongquan.com.quantaxi.Utils.Common;

public class WellcomeActivity extends AppCompatActivity {

	Button btnDriver;
	Button btnCustomer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wellcome);

		btnDriver = findViewById(R.id.btnDriver_wellcome);
		btnCustomer = findViewById(R.id.btnCustomer_wellcome);

		btnDriver.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intentToDriver = new Intent(WellcomeActivity.this, LoginActivity.class);
				intentToDriver.putExtra(Common.LOGIN_INTENT, Common.DRIVER_LOGIN_TITLE);
				startActivity(intentToDriver);

			}
		});

		btnCustomer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intentToCustomer = new Intent(WellcomeActivity.this, LoginActivity.class);
				intentToCustomer.putExtra(Common.LOGIN_INTENT, Common.CUSTOMER_LOGIN_TITLE);
				startActivity(intentToCustomer);
			}
		});

	}
}
