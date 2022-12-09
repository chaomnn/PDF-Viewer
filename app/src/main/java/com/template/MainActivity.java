package com.template;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.template.databinding.ActivityMainBinding;
import com.template.fragments.ContentFragment;
import com.template.fragments.SplashScreenFragment;

public class MainActivity extends AppCompatActivity implements SplashScreenFragment.Callback {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(getMainFragmentId(), SplashScreenFragment.class, null)
                    .commitNow();
        }
    }

    private int getMainFragmentId() {
        return binding.mainFragment.getId();
    }

    @Override
    public void onAnimationEnd() {
        getSupportFragmentManager().beginTransaction().replace(getMainFragmentId(), ContentFragment.class, null)
                .commitNow();
    }
}
