package es.exsample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class SubExSample extends FragmentActivity implements OnMapReadyCallback {

    SupportMapFragment mf;
    LocationManager lm;
    private final int REQUEST_PERMISSION_1 = 1;
    private final int REQUEST_PERMISSION_2 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mf = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, mf)
                .commit();
        mf.getMapAsync(this);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  //ロケーションマネージャをシステムから取得
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SubExSample.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_1);
            ActivityCompat.requestPermissions(SubExSample.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_2);
            return;
        } //Dangerous Permission の許可

        map.setMyLocationEnabled(true);

        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60 * 3 * 1000, 50, new LocationListener() {
            @Override
            public void onLocationChanged(Location lc) {
                double lt = lc.getLatitude();  //緯度の取得
                double ln = lc.getLongitude();  //経度の取得
                LatLng ltn = new LatLng(lt, ln);  //緯度、経度情報を扱うためのオブジェクト生成
                map.moveCamera(CameraUpdateFactory.newLatLng(ltn));  //マップ表示のためのカメラ移動
                map.moveCamera(CameraUpdateFactory.zoomTo(15));  //マップ表示のためのズーム変更
                Toast.makeText(getBaseContext(), "現在地は\n緯度:" + lt + "\n経度:" + ln + "です。", Toast.LENGTH_LONG).show();  //緯度と経度をトースト表示
            }

            @Override
            public void onProviderDisabled(String pv) {
            }

            @Override
            public void onProviderEnabled(String pv) {
            }

            @Override
            public void onStatusChanged(String pv, int status, Bundle ex) {
            }
        });  //位置情報更新時のリスナー登録と、位置情報ソース、時間、距離間隔の設定

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng ltn) {
                MarkerOptions mo = new MarkerOptions();  //マップに表示するマーカーの生成
                mo.position(ltn);  //マーカに緯度と経度を設定
                mo.title("緯度は");  //マーカにタイトルを設定
                String str = String.valueOf(ltn.latitude);
                mo.snippet(str);  //マーカにテキストを設定
                map.addMarker(mo);  //マップにマーカーをセット
            }
        });  //マップクリック時のリスナー登録
    }
}
