package com.template.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.vectordrawable.graphics.drawable.AnimatorInflaterCompat;
import com.template.R;
import com.template.databinding.FragmentSplashScreenBinding;

@SuppressLint("CustomSplashScreen")
public class SplashScreenFragment extends Fragment {

    public interface Callback {
        void onAnimationEnd();
    }

    public Callback getCallback() {
        return (Callback) requireContext();
    }

    public SplashScreenFragment() {
        super(R.layout.fragment_splash_screen);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final FragmentSplashScreenBinding binding = FragmentSplashScreenBinding.bind(view);
        final Animator animator = AnimatorInflater.loadAnimator(requireContext(), R.animator.zoom_in);
        animator.setTarget(binding.splashLogo);
        animator.start();

        long targetTime = SystemClock.elapsedRealtime() + 1500;
        Runnable endCallback = () -> getCallback().onAnimationEnd();

        getViewLifecycleOwner().getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_START) {
                long time = targetTime - SystemClock.elapsedRealtime();
                view.postDelayed(endCallback, time);
            } else if (event == Lifecycle.Event.ON_STOP) {
                view.removeCallbacks(endCallback);
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                animator.cancel();
            }
        });
    }
}
