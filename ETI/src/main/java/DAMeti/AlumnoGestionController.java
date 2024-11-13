package DAMeti;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.sql.*;

import Modelo.Alumno;
import sql.conexion;

public class AlumnoGestionController {

    @FXML
    private TextField txtBuscarID;
    @FXML
    private TableView<Alumno> tablaUsuarios;
    @FXML
    private TableColumn<Alumno, Integer> colId;
    @FXML
    private TableColumn<Alumno, String> colNombre;
    @FXML
    private TableColumn<Alumno, String> colPadreMadre;
    @FXML
    private TableColumn<Alumno, Integer> colCurso;
    @FXML
    private TableColumn<Alumno, String> colTutor;
    @FXML
    private TableColumn<Alumno, String> colUsuario;
    @FXML
    private Label lblMensaje;

    private ObservableList<Alumno> listaAlumnos;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCurso.setCellValueFactory(new PropertyValueFactory<>("curso"));
        colPadreMadre.setCellValueFactory(new PropertyValueFactory<>("nombreMadrePadre"));
        colTutor.setCellValueFactory(new PropertyValueFactory<>("tutor"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        cargarDatos();
    }

    void cargarDatos() {
        listaAlumnos = FXCollections.observableArrayList();
        String url = "jdbc:mysql://localhost:3306/eti"; 
        try (Connection connection = conexion.dameConexion(); 
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM alumnos")) {

            while (resultSet.next()) {
                Alumno alumno = new Alumno(
                        resultSet.getInt("id"),
                        resultSet.getString("nombre"),
                        resultSet.getInt("curso"),
                        resultSet.getString("nombre_madre_padre"),
                        resultSet.getString("nombre_tutor"),
                        resultSet.getString("usuario"),
                        null // No se usa el password aquí
                );
                listaAlumnos.add(alumno);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblMensaje.setText("Error al cargar los datos.");
        }
        tablaUsuarios.setItems(listaAlumnos);
    }

    @FXML
    void buscarPorID() {
        String buscarID = txtBuscarID.getText();
        if (buscarID.isEmpty()) {
            lblMensaje.setText("Por favor, ingrese un ID para buscar.");
            return;
        }

        for (Alumno alumno : listaAlumnos) {
            if (String.valueOf(alumno.getId()).equals(buscarID)) {
                tablaUsuarios.getSelectionModel().select(alumno);
                return;
            }
        }
        lblMensaje.setText("Alumno no encontrado.");
    }

    @FXML
    void modificarUsuario() {
        Alumno alumnoSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (alumnoSeleccionado == null) {
            lblMensaje.setText("Seleccione un alumno para modificar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DAM/ETI/AlumnoForm.fxml"));
            Parent root = loader.load();

            AlumnoFormControlador controlador = loader.getController();
            controlador.initAttributes(alumnoSeleccionado);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

            Alumno alumnoModificado = controlador.getAlumno();
            if (alumnoModificado != null) {
                alumnoSeleccionado.setNombre(alumnoModificado.getNombre());
                alumnoSeleccionado.setCurso(alumnoModificado.getCurso());
                alumnoSeleccionado.setNombreMadrePadre(alumnoModificado.getNombreMadrePadre());
                alumnoSeleccionado.setTutor(alumnoModificado.getTutor());
                alumnoSeleccionado.setUsuario(alumnoModificado.getUsuario());
                actualizarAlumnoEnBD(alumnoSeleccionado);
                tablaUsuarios.refresh();
                
                // Crear la alerta
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Modificación Exitosa");
                alerta.setHeaderText(null);
                alerta.setContentText("Alumno modificado correctamente.");
                alerta.showAndWait();
            } else {
                // Si no se pudo obtener el alumno, mostrar un error
                Alert alertaError = new Alert(Alert.AlertType.ERROR);
                alertaError.setTitle("Error");
                alertaError.setHeaderText(null);
                alertaError.setContentText("No se pudo modificar el alumno.");
                alertaError.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
            lblMensaje.setText("Error al cargar el formulario de modificación.");
        }
    }

    private void actualizarAlumnoEnBD(Alumno alumno) {
        String sql = "UPDATE alumnos SET nombre = ?, curso = ?, nombre_madre_padre = ?, nombre_tutor = ?, usuario = ? WHERE id = ?";
        try (Connection connection = conexion.dameConexion();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, alumno.getNombre());
            preparedStatement.setInt(2, alumno.getCurso());
            preparedStatement.setString(3, alumno.getNombreMadrePadre());
            preparedStatement.setString(4, alumno.getTutor());
            preparedStatement.setString(5, alumno.getUsuario());
            preparedStatement.setInt(6, alumno.getId());
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                lblMensaje.setText("");
            } else {
                lblMensaje.setText("No se encontró el alumno para actualizar.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblMensaje.setText("Error al actualizar el alumno en la base de datos.");
        }
    }

    @FXML
    void eliminarUsuario() {
        Alumno alumnoSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (alumnoSeleccionado == null) {
            lblMensaje.setText("Seleccione un alumno para eliminar.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de que desea eliminar este alumno?");
        alert.setHeaderText(null);
        alert.setTitle("Confirmar eliminación");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                eliminarAlumno(alumnoSeleccionado.getId());
                
                // Alerta de eliminación exitosa
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Eliminación Exitosa");
                alerta.setHeaderText(null);
                alerta.setContentText("Alumno eliminado correctamente.");
                alerta.showAndWait();
            } else {
                // Si la eliminación es cancelada, no se muestra ningún mensaje adicional
            }
            cargarDatos();
        });
    }

    private void eliminarAlumno(int id) {
        String sql = "DELETE FROM alumnos WHERE id = ?";
        try (Connection connection = conexion.dameConexion();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // Agregar una alerta en caso de error en la base de datos
            Alert alertaError = new Alert(Alert.AlertType.ERROR);
            alertaError.setTitle("Error de eliminación");
            alertaError.setHeaderText(null);
            alertaError.setContentText("No se pudo eliminar el alumno.");
            alertaError.showAndWait();
        }
    }


    @FXML
    private void handleBackButtonAction(ActionEvent event) {
        App.changeScene((Stage) ((Node) event.getSource()).getScene().getWindow(), "/DAM/ETI/admin2.fxml");
    }

    @FXML
    private void handleInicioButtonAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DAM/ETI/inicio.fxml"));
        Parent inicioView = loader.load();
        Scene inicioScene = new Scene(inicioView);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(inicioScene);
        stage.show();
    }
}
