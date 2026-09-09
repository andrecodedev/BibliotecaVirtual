package com.example.ac1_andre_gabriela;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText edtTitulo, edtAutor;
    private RadioGroup radioLeitura;
    private RadioButton radioLido, radioNaoLido;
    private Button btnSalvar;
    private ListView listViewLivros;
    private BancoHelper databaseHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listaLivros;
    private ArrayList<Integer> listaIds;
    private int livroEditandoId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtTitulo = findViewById(R.id.edtTitulo);
        edtAutor = findViewById(R.id.edtAutor);
        radioLeitura = findViewById(R.id.radioLeitura);
        radioLido = findViewById(R.id.radioLido);
        radioNaoLido = findViewById(R.id.radioNaoLido);
        btnSalvar = findViewById(R.id.btnSalvar);
        listViewLivros = findViewById(R.id.listViewLivros);
        databaseHelper = new BancoHelper(this);

        carregarLivros();

        btnSalvar.setOnClickListener(v -> salvarOuAtualizar());

        listViewLivros.setOnItemClickListener((parent, view, position, id) -> {
            livroEditandoId = listaIds.get(position);
            String[] partes = listaLivros.get(position).split(" - ");
            edtTitulo.setText(partes[1]);
            edtAutor.setText(partes[2]);
            if (partes[3].equals("Lido")) {
                radioLido.setChecked(true);
            } else {
                radioNaoLido.setChecked(true);
            }
            btnSalvar.setText("Atualizar");
        });

        listViewLivros.setOnItemLongClickListener((parent, view, position, id) -> {
            int idLivro = listaIds.get(position);
            int deletado = databaseHelper.excluirLivro(idLivro);
            if (deletado > 0) {
                Toast.makeText(this, "Livro excluído", Toast.LENGTH_SHORT).show();
                limparFormulario();
                carregarLivros();
            }
            return true;
        });
    }

    private void salvarOuAtualizar() {
        String titulo = edtTitulo.getText().toString().trim();
        String autor = edtAutor.getText().toString().trim();
        int selecionado = radioLeitura.getCheckedRadioButtonId();

        if (titulo.isEmpty() || autor.isEmpty()) {
            Toast.makeText(this, "Preencha título e autor", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selecionado == -1) {
            Toast.makeText(this, "Informe se a leitura foi concluída", Toast.LENGTH_SHORT).show();
            return;
        }

        int lido = (selecionado == R.id.radioLido) ? 1 : 0;

        if (livroEditandoId == -1) {
            long resultado = databaseHelper.inserirLivro(titulo, autor, lido);
            if (resultado != -1) {
                Toast.makeText(this, "Livro salvo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            int resultado = databaseHelper.atualizarLivro(livroEditandoId, titulo, autor, lido);
            if (resultado > 0) {
                Toast.makeText(this, "Livro atualizado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        limparFormulario();
        carregarLivros();
    }

    private void carregarLivros() {
        Cursor cursor = databaseHelper.listarLivros();
        listaLivros = new ArrayList<>();
        listaIds = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String titulo = cursor.getString(1);
                String autor = cursor.getString(2);
                int lido = cursor.getInt(3);
                String status = (lido == 1) ? "Lido" : "Nao lido";
                listaLivros.add(id + " - " + titulo + " - " + autor + " - " + status);
                listaIds.add(id);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaLivros);
        listViewLivros.setAdapter(adapter);
    }

    private void limparFormulario() {
        edtTitulo.setText("");
        edtAutor.setText("");
        radioLeitura.clearCheck();
        livroEditandoId = -1;
        btnSalvar.setText("Salvar");
    }
}
