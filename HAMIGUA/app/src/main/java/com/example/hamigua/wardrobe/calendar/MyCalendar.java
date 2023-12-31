package com.example.hamigua.wardrobe.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamigua.R;
import com.example.hamigua.wardrobe.dressRecord.CalendarRecord;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MyCalendar extends AppCompatActivity implements CalendarAdapter.OnItemListener
{
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private ArrayList<String> record_Days;

    //標題列ActionBar的左上角返回鍵監聽事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //取得firestore與authentication的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();
    }

    //初始化layout物件
    private void initWidgets()
    {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
    }

    private void setMonthView()
    {
        monthYearText.setText(monthYearFromDate(selectedDate));

        record_Days = new ArrayList<>();

        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Calendar_Record")
                .whereEqualTo("record_YearMonth", monthYearFromDate(selectedDate))
                .orderBy("record_Date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                record_Days.add(document.getString("record_DaysInMonth"));
                            }

                            ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

                            CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, MyCalendar.this, record_Days);
                            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
                            calendarRecyclerView.setLayoutManager(layoutManager);
                            calendarRecyclerView.setAdapter(calendarAdapter);
                        }
                    }
                });
    }

    private ArrayList<String> daysInMonthArray(LocalDate date)
    {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 1; i <= 42; i++)
        {
            if(i <= dayOfWeek || i > daysInMonth + dayOfWeek)
            {
                daysInMonthArray.add("");
            }
            else
            {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return  daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
        return date.format(formatter);
    }

    public void previousMonthAction(View view)
    {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view)
    {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText)
    {
        if(!dayText.equals(""))
        {
            String message = monthYearFromDate(selectedDate) + "/" + dayText;

            Intent intent_Calendarrecord = new Intent();
            intent_Calendarrecord.setClass(MyCalendar.this, CalendarRecord.class);
            intent_Calendarrecord.putExtra("sDate", message);
            startActivity(intent_Calendarrecord);

        }
    }

}
