package bibliotecaudb.dao.biblioteca.impl;

import bibliotecaudb.modelo.biblioteca.PoliticasPrestamo;
import bibliotecaudb.modelo.usuario.TipoUsuario; 
import bibliotecaudb.dao.biblioteca.PoliticasPrestamoDAO;
import bibliotecaudb.dao.usuario.TipoUsuarioDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.dao.usuario.impl.TipoUsuarioDAOImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PoliticasPrestamosDAOImpl implements PoliticasPrestamoDAO {

    private static final String SQL_INSERT = "INSERT INTO politicas_prestamo (id_tipo_usuario, max_ejemplares_prestamo, dias_prestamo_default) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE_BY_ID_TIPO_USUARIO = "UPDATE politicas_prestamo SET max_ejemplares_prestamo = ?, dias_prestamo_default = ? WHERE id_tipo_usuario = ?";
    private static final String SQL_SELECT_BY_ID_TIPO_USUARIO = "SELECT id_politica, id_tipo_usuario, max_ejemplares_prestamo, dias_prestamo_default FROM politicas_prestamo WHERE id_tipo_usuario = ?";
    private static final String SQL_SELECT_BY_ID_POLITICA = "SELECT id_politica, id_tipo_usuario, max_ejemplares_prestamo, dias_prestamo_default FROM politicas_prestamo WHERE id_politica = ?";
    private static final String SQL_SELECT_ALL = "SELECT id_politica, id_tipo_usuario, max_ejemplares_prestamo, dias_prestamo_default FROM politicas_prestamo ORDER BY id_tipo_usuario";

    private TipoUsuarioDAO tipoUsuarioDAO; // Para cargar el objeto TipoUsuario

    public PoliticasPrestamosDAOImpl() {
        // Para obtener el objeto TipoUsuario completo al mapear PoliticasPrestamo
        this.tipoUsuarioDAO = new TipoUsuarioDAOImpl();
    }

    // Constructor para permitir la inyección de dependencias, útil para pruebas
    public PoliticasPrestamosDAOImpl(TipoUsuarioDAO tipoUsuarioDAO) {
        this.tipoUsuarioDAO = tipoUsuarioDAO;
    }

    @Override
    public boolean insertar(PoliticasPrestamo politica) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, politica.getIdTipoUsuario());
            pstmt.setInt(2, politica.getMaxEjemplaresPrestamo());
            pstmt.setInt(3, politica.getDiasPrestamoDefault());

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_INSERT);
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    politica.setIdPolitica(generatedKeys.getInt(1));
                }
                LogsError.info(this.getClass(), "Política de préstamo insertada con ID: " + politica.getIdPolitica() + " para tipo_usuario ID: " + politica.getIdTipoUsuario());
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar política de préstamo: " + ex.getMessage(), ex);
            // Puede fallar si se viola la UNIQUE KEY en id_tipo_usuario
            throw ex;
        } finally {
            ConexionBD.close(generatedKeys);
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    @Override
    public boolean actualizarPorIdTipoUsuario(PoliticasPrestamo politica) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE_BY_ID_TIPO_USUARIO);
            pstmt.setInt(1, politica.getMaxEjemplaresPrestamo());
            pstmt.setInt(2, politica.getDiasPrestamoDefault());
            pstmt.setInt(3, politica.getIdTipoUsuario()); // Condición WHERE

            LogsError.info(this.getClass(), "Actualizando política para tipo_usuario ID: " + politica.getIdTipoUsuario());
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Política de préstamo actualizada. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontró política para actualizar para tipo_usuario ID: " + politica.getIdTipoUsuario() + " o los valores eran los mismos.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar política de préstamo por id_tipo_usuario: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }
    
    private PoliticasPrestamo mapearResultSet(ResultSet rs) throws SQLException {
        PoliticasPrestamo pp = new PoliticasPrestamo();
        pp.setIdPolitica(rs.getInt("id_politica"));
        pp.setIdTipoUsuario(rs.getInt("id_tipo_usuario"));
        pp.setMaxEjemplaresPrestamo(rs.getInt("max_ejemplares_prestamo"));
        pp.setDiasPrestamoDefault(rs.getInt("dias_prestamo_default"));

        // Cargar el objeto TipoUsuario anidado usando el DAO inyectado/instanciado
        if (this.tipoUsuarioDAO != null) {
            TipoUsuario tu = this.tipoUsuarioDAO.obtenerPorId(pp.getIdTipoUsuario());
            pp.setTipoUsuario(tu); // Asigna el objeto TipoUsuario completo
        }
        return pp;
    }

    @Override
    public PoliticasPrestamo obtenerPorIdTipoUsuario(int idTipoUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PoliticasPrestamo politica = null;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID_TIPO_USUARIO);
            pstmt.setInt(1, idTipoUsuario);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ID_TIPO_USUARIO + " con idTipoUsuario: " + idTipoUsuario);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                politica = mapearResultSet(rs);
            } else {
                LogsError.warn(this.getClass(), "No se encontró política de préstamo para idTipoUsuario: " + idTipoUsuario);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener política por idTipoUsuario: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return politica;
    }

    @Override
    public PoliticasPrestamo obtenerPorIdPolitica(int idPolitica) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PoliticasPrestamo politica = null;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID_POLITICA);
            pstmt.setInt(1, idPolitica);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ID_POLITICA + " con idPolitica: " + idPolitica);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                politica = mapearResultSet(rs);
            } else {
                LogsError.warn(this.getClass(), "No se encontró política de préstamo para idPolitica: " + idPolitica);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener política por idPolitica: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return politica;
    }
    
    @Override
    public List<PoliticasPrestamo> obtenerTodas() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<PoliticasPrestamo> politicas = new ArrayList<>();
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                politicas.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todas las políticas de préstamo: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return politicas;
    }
}