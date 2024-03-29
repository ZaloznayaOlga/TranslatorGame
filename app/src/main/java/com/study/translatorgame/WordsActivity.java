package com.study.translatorgame;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class WordsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewWords;
    public static final ArrayList<Word> words = new ArrayList<>();
    private SQLiteDatabase database;
    private SQLiteOpenHelper dbHelper;
    private Cursor cursor;
    private WordsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerViewWords = findViewById(R.id.recyclerViewWords);
        dbHelper = new DBWordsHelper(this);

        try {
            database = dbHelper.getWritableDatabase();
            getData();
        } catch (SQLiteException e) {
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show();
        }

        adapter = new WordsAdapter(words);
        recyclerViewWords.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewWords.setAdapter(adapter);
        adapter.setClickListener(new WordsAdapter.OnWordClickListener() {
            @Override
            public void onWordClick(int position) {
                //быстрое нажатие
            }

            @Override
            public void onLongClick(int position) {
                //долгое нажатие
                //реализовать редактирование записи
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                //удаление элемента свайпом влево
                remove(viewHolder.getAdapterPosition());
                Toast.makeText(getApplicationContext(), "Элемент удален!", Toast.LENGTH_SHORT).show();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerViewWords);
    }

    private void remove(int position) {
        int id = words.get(position).getId();
        String where = DBWordsContract.WordsEntry._ID + " = ?";
        String[] whereArgs = new String[]{Integer.toString(id)};
        database.delete(DBWordsContract.WordsEntry.TABLE_NAME, where, whereArgs);
        try {
            getData();
        } catch (SQLiteException e) {
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_start_game) {
            if (words.size() > 4) {
                startActivity(new Intent(this, GameActivity.class));
            } else Toast.makeText(this, "Добавьте более 4 слов!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getData() {
        if (!words.isEmpty()) words.clear();

        cursor = database.query(DBWordsContract.WordsEntry.TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(DBWordsContract.WordsEntry._ID));
            String word = cursor.getString(cursor.getColumnIndex(DBWordsContract.WordsEntry.COLUMN_WORD));
            String translation = cursor.getString(cursor.getColumnIndex(DBWordsContract.WordsEntry.COLUMN_TRANSLATION));

            words.add(new Word(id, word, new ArrayList<>(Arrays.asList(translation.split("\\s*,\\s*")))));
        }
    }

    public void onClickAddWord(View view) {
        startActivity(new Intent(this, AddWordActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursor.close();
        database.close();
    }
}
