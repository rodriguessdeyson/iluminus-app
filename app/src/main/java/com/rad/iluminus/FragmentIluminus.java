package com.rad.iluminus;

import android.animation.Animator;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.material.appbar.AppBarLayout;
import com.rad.adapters.BluetoothDeviceListViewAdapter;
import com.rad.iluminus.databinding.FragmentIluminusBinding;
import com.rad.iluminus.databinding.FragmentMainBinding;
import com.rad.iluminus.databinding.FragmentPermissionBinding;
import com.rad.iluminus.databinding.FragmentSearchDeviceBinding;
import com.rad.models.BluetoothDeviceModel;

public class FragmentIluminus extends Fragment {

	private FragmentIluminusBinding binding;
	private BluetoothDeviceModel bluetoothDeviceModel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			this.bluetoothDeviceModel = getArguments().getParcelable("bluetoothDeviceModel");
		}
	}

	@Override
	public View onCreateView(
		@NonNull LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState)
	{
		binding = FragmentIluminusBinding.inflate(inflater, container, false);
		z
		binding.ColorPickerView.setOnTouchListener(v, motionEvent -> {
		
		});
		
		return binding.getRoot();
	}

	private void disableCollapsing() {
		AppBarLayout layout = requireActivity().findViewById(R.id.app_bar);
		View child = layout.getChildAt(0);
		AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) child.getLayoutParams();
		params.height = 56;
		params.setScrollFlags(0);
		
		binding.NestedScrollView.scrollTo(0,0);
	}
}