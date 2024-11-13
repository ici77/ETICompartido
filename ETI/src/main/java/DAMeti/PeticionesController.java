package DAMeti;

import Modelo.Peticion;
import Modelo.Prestamo;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Date;
import java.time.LocalDate;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class PeticionesController {

    @FXML
    private TableView<Peticion> tablaPeticiones;

    @FXML
    private TableColumn<Peticion, Integer> colId;

    @FXML
    private TableColumn<Peticion, String> colNombreAlumno;

    @FXML
    private TableColumn<Peticion, Integer> colCurso;

    @FXML
    private TableColumn<Peticion, Integer> colIdLibro;

    @FXML
    private TableColumn<Peticion, String> colTituloLibro;

    @FXML
    private TableColumn<Peticion, String> colIsbnLibro;

    @FXML
    private TableColumn<Peticion, String> colFechaPeticion;

    @FXML
    private TableColumn<Peticion, Integer> colNumeroCopias;

    @FXML
    private Button cerrarButton;

    @FXML
    private Button validarPrestamoButton;

    private ObservableList<Peticion> listaPeticiones;
    ObservableList<Prestamo> listaPrestamos = FXCollections.observableArrayList();


    @FXML
    private TextField searchIdField;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreAlumno.setCellValueFactory(new PropertyValueFactory<>("nombreAlumno"));
        colCurso.setCellValueFactory(new PropertyValueFactory<>("curso"));
        colIdLibro.setCellValueFactory(new PropertyValueFactory<>("idLibro"));
        colTituloLibro.setCellValueFactory(new PropertyValueFactory<>("tituloLibro"));
        colIsbnLibro.setCellValueFactory(new PropertyValueFactory<>("isbnLibro"));
        colFechaPeticion.setCellValueFactory(new PropertyValueFactory<>("fechaPeticion"));
        colNumeroCopias.setCellValueFactory(new PropertyValueFactory<>("numeroCopias"));

        cargarPeticiones();
    }

    void cargarPeticiones() {
        listaPeticiones = FXCollections.observableArrayList();
        String url = "jdbc:mysql://localhost:3306/eti";
        String sql = "SELECT * FROM peticiones";

        try (Connection connection = DriverManager.getConnection(url, "root", "");
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet resultSet = stmt.executeQuery()) {

            listaPeticiones.clear();

            while (resultSet.next()) {
                int id = resultSet.getInt("id_peticiones");
                String nombreAlumno = resultSet.getString("nombre_alumno");
                int curso = resultSet.getInt("curso");
                int idLibro = resultSet.getInt("id_libro");
                String tituloLibro = resultSet.getString("titulo_libro");
                String isbnLibro = resultSet.getString("isbn_libro");
                String fechaPeticionStr = resultSet.getString("fecha_peticion");
                LocalDate fechaPeticion = fechaPeticionStr != null && !fechaPeticionStr.isEmpty() ? LocalDate.parse(fechaPeticionStr) : null;
                int numeroCopias = resultSet.getInt("numero_copias");

                listaPeticiones.add(new Peticion(id, nombreAlumno, curso, idLibro, tituloLibro, isbnLibro, fechaPeticion, numeroCopias));
            }

            tablaPeticiones.setItems(listaPeticiones);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar las peticiones", "No se pudo cargar las peticiones desde la base de datos.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    @FXML
    private void buscarPorId(ActionEvent event) {
        String id = searchIdField.getText(); // Obtener el ID ingresado por el usuario

        if (id.isEmpty()) {
            mostrarAlerta("Error", "Por favor ingrese un ID para buscar.");
            return;
        }

        listaPeticiones.clear(); // Limpiar la lista antes de cargar nuevos resultados

        String url = "jdbc:mysql://localhost:3306/eti"; // Asegúrate de que la URL sea correcta
        String sql = "SELECT * FROM peticiones WHERE id_peticiones = ?"; // Consulta SQL para buscar por ID

        try (Connection connection = DriverManager.getConnection(url, "root", "");
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, id); 

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    // Leer los datos de la base de datos
                    int peticionId = resultSet.getInt("id_peticiones");
                    String nombreAlumno = resultSet.getString("nombre_alumno");
                    int curso = resultSet.getInt("curso");
                    int idLibro = resultSet.getInt("id_libro");
                    String tituloLibro = resultSet.getString("titulo_libro");
                    String isbnLibro = resultSet.getString("isbn_libro");

                    // Convertir la fecha de String a LocalDate
                    String fechaPeticionStr = resultSet.getString("fecha_peticion");
                    LocalDate fechaPeticion = null;
                    if (fechaPeticionStr != null && !fechaPeticionStr.isEmpty()) {
                        fechaPeticion = LocalDate.parse(fechaPeticionStr);  // Parseamos la fecha a LocalDate
                    }

                    int numeroCopias = resultSet.getInt("numero_copias");

                    // Crear una nueva instancia de Peticion y añadirla a la lista
                    listaPeticiones.add(new Peticion(peticionId, nombreAlumno, curso, idLibro, tituloLibro, isbnLibro, fechaPeticion, numeroCopias));
                }
            }

            // Establecer los elementos en la tabla
            tablaPeticiones.setItems(listaPeticiones);

            // Si no se encontró ninguna petición, mostrar la alerta
            if (listaPeticiones.isEmpty()) {
                mostrarAlerta("No se encontró", "No se encontró ninguna petición con ese ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Mostrar alerta de error si no se puede realizar la búsqueda
            mostrarAlerta("Error al buscar", "Hubo un error al realizar la búsqueda.");
        }
    }


    @FXML
    private void handleValidarPrestamo(ActionEvent event) {
        Peticion peticionSeleccionada = tablaPeticiones.getSelectionModel().getSelectedItem();

        if (peticionSeleccionada == null) {
            mostrarAlerta("Error", "Por favor, seleccione una petición para validar.");
            return;
        }

        LocalDate fechaPeticion = peticionSeleccionada.getFechaPeticion();
        if (fechaPeticion == null) {
            mostrarAlerta("Error", "La petición no tiene una fecha válida.");
            return;
        }

        LocalDate fechaDevolucion = fechaPeticion.plusDays(15);
        java.sql.Date sqlFechaPrestamo = java.sql.Date.valueOf(fechaPeticion);
        java.sql.Date sqlFechaDevolucion = java.sql.Date.valueOf(fechaDevolucion);

        // Crear el objeto Prestamo con numeroCopias
        int numeroCopias = peticionSeleccionada.getNumeroCopias(); 
        Prestamo prestamo = new Prestamo(
            peticionSeleccionada.getId(),
            peticionSeleccionada.getIdLibro(),
            peticionSeleccionada.getNombreAlumno(),
            peticionSeleccionada.getTituloLibro(),
            sqlFechaPrestamo,
            sqlFechaDevolucion,
            numeroCopias
        );

        // Primero, actualizar el número de copias del libro
        String updateLibroSQL = "UPDATE libros SET numero_de_copias = numero_de_copias - ? WHERE id = ?";
        String insertPrestamoSQL = "INSERT INTO prestamos (id_peticion, id_libro, nombre_alumno, titulo_libro, fecha_prestamo, fecha_devolucion, numero_copias) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eti", "root", "");
             PreparedStatement updateStmt = connection.prepareStatement(updateLibroSQL);
             PreparedStatement insertStmt = connection.prepareStatement(insertPrestamoSQL)) {

            // Actualizar el número de copias en el libro
            updateStmt.setInt(1, numeroCopias);
            updateStmt.setInt(2, prestamo.getIdLibro());
            int rowsUpdated = updateStmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Número de copias actualizado correctamente.");
            } else {
                System.out.println("No se pudo encontrar el libro con el id: " + prestamo.getIdLibro());
                mostrarAlerta("Error", "No se pudo encontrar el libro con el ID especificado.");
                return;
            }

            // Luego, insertar el préstamo en la base de datos
            insertStmt.setInt(1, prestamo.getIdPeticion());
            insertStmt.setInt(2, prestamo.getIdLibro());
            insertStmt.setString(3, prestamo.getNombreAlumno());
            insertStmt.setString(4, prestamo.getTituloLibro());
            insertStmt.setDate(5, sqlFechaPrestamo);
            insertStmt.setDate(6, sqlFechaDevolucion);
            insertStmt.setInt(7, prestamo.getNumeroCopias());
            insertStmt.executeUpdate();

            // Eliminar la petición procesada
            eliminarPeticion(peticionSeleccionada.getId());

            // Cargar la lista de préstamos después de la actualización y asegurarnos que la tabla de préstamos se cargue correctamente
            cargarPrestamos();

            mostrarAlerta("Éxito", "El préstamo ha sido validado con éxito.");
            mostrarConfirmacion(event);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al validar préstamo", "Hubo un error al validar el préstamo.");
        }
    }



    private void eliminarPeticion(int id) {
        String sql = "DELETE FROM peticiones WHERE id_peticiones = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eti", "root", "");
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            mostrarAlerta("Éxito", "La petición ha sido eliminada tras la validación.");
            cargarPeticiones(); // Recargar la lista de peticiones después de eliminar
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar", "Hubo un error al eliminar la petición.");
        }
    }

    private void cargarPrestamos() {
        ObservableList<Prestamo> listaPrestamos = FXCollections.observableArrayList();
        String url = "jdbc:mysql://localhost:3306/eti";
        String sql = "SELECT * FROM prestamos";

        try (Connection connection = DriverManager.getConnection(url, "root", "");
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet resultSet = stmt.executeQuery()) {

            listaPrestamos.clear();

            while (resultSet.next()) {
                int idPeticion = resultSet.getInt("id_peticion");
                int idLibro = resultSet.getInt("id_libro");
                String nombreAlumno = resultSet.getString("nombre_alumno");
                String tituloLibro = resultSet.getString("titulo_libro");
                Date fechaPrestamo = resultSet.getDate("fecha_prestamo");
                Date fechaDevolucion = resultSet.getDate("fecha_devolucion");

                
                String sqlCopiasLibro = "SELECT numero_de_copias FROM libros WHERE id = ?";
                try (PreparedStatement stmtCopias = connection.prepareStatement(sqlCopiasLibro)) {
                    stmtCopias.setInt(1, idLibro);
                    ResultSet rsCopias = stmtCopias.executeQuery();
                    if (rsCopias.next()) {
                        int numeroCopias = rsCopias.getInt("numero_de_copias");

                     
                        listaPrestamos.add(new Prestamo(idPeticion, idLibro, nombreAlumno, tituloLibro, fechaPrestamo, fechaDevolucion, numeroCopias));
                    }
                }

            }

          

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar los préstamos", "No se pudo cargar los préstamos desde la base de datos.");
        }
    }



    private void mostrarConfirmacion(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("El préstamo ha sido validado.");
        alert.setContentText("¿Deseas ver la tabla de préstamos?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            irAVista("/DAM/ETI/Prestamo.fxml", event);  
        } else {
            irAVista("/DAM/ETI/inicio.fxml", event);  
        }
    }

    private void irAVista(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) cerrarButton.getScene().getWindow();
        stage.close();
    }
}
