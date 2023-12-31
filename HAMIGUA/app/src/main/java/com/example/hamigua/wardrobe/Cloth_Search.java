package com.example.hamigua.wardrobe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.hamigua.R;
import com.example.hamigua.adapter.RecyclerViewAdapter;

import java.util.ArrayList;

public class Cloth_Search extends AppCompatActivity {
    RecyclerViewAdapter mAdapter;
    String[] strings = {"CRUISER MK. I","CRUISER MK. II","VALENTINE","CRUISER MK. III"
            ,"CRUISER MK. IV","COVENANTER","CRUSADER","GSR 3301 SETTER","LHMTV","GSOR3301 AVR FS"
            ,"MANTICORE","MATILDA","CHURCHILL I","CHURCHILL VII","BLACK PRINCE","CAERNARVON"
            ,"CONQUEROR","SUPER CONQUEROR","CAVALIER","CROMWELL","COMET","CENTURION MK. I"
            ,"CENTURION MK. 7/1","CENTURION ACTION X","VALENTINE AT","BISHOP","FV304"
            ,"CRUSADER 5.5-IN. SP","FV207","FV3805","CONQUEROR GUN CARRIAGE"
            ,"AT 2","ACHILLES","CHALLENGER","CHARIOTEER","FV4004 CONWAY","FV4005 STAGE II"
            ,"AT 8","AT 7","AT 15","TORTOISE","FV217 BADGER"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_search);


            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < strings.length; i++) {
                arrayList.add(strings[i]);
            }
            mAdapter = new RecyclerViewAdapter(arrayList);
            recyclerView.setAdapter(mAdapter);

        }

        /**初始化Toolbar內SearchView的設置*/
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {

            getMenuInflater().inflate(R.menu.search_menu, menu);
            MenuItem menuItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) menuItem.getActionView();
            /**SearchView設置，以及輸入內容後的行動*/
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    /**調用RecyclerView內的Filter方法*/
                    mAdapter.getFilter().filter(newText);
                    return false;
                }
            });
            return super.onCreateOptionsMenu(menu);
        }
    }
