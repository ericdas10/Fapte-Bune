package examen.fapte_bune.repository;

import examen.fapte_bune.domain.Oras;
import examen.fapte_bune.domain.Persoana;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersoaneRepo implements Repository<Long, Persoana> {
    private final Connection connection;

    public PersoaneRepo() throws SQLException {
        try{
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/faptebune","postgres","ericdas777");
            try (Statement statement = connection.createStatement()) {
                // Ensure the users table exists
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS persoane(" +
                                "persoanaId SERIAL PRIMARY KEY," +
                                "nume VARCHAR(50)," +
                                "prenume VARCHAR(50)," +
                                "username VARCHAR(50) UNIQUE," +
                                "parola varchar(50)," +
                                "oras VARCHAR(50)," +
                                "strada VARCHAR(50)," +
                                "numarStrada VARCHAR(10)," +
                                "telefon VARCHAR(14));"

                );
            }

        }catch (SQLException e){
            throw new SQLException("Error initializing the database", e);
        }
    }



    @Override
    public Optional<Persoana> findOne(Long id) throws IOException{
        String sql = "SELECT * FROM persoane WHERE persoanaId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String nume = resultSet.getString("nume");
                String prenume = resultSet.getString("prenume");
                String username = resultSet.getString("username");
                String parola = resultSet.getString("parola");
                Oras oras = Oras.valueOf(resultSet.getString("oras"));
                String strada = resultSet.getString("strada");
                String numarStrada = resultSet.getString("numarStrada");
                String telefon = resultSet.getString("telefon");
                return Optional.of(new Persoana(id, nume, prenume, username, parola, oras, strada, numarStrada, telefon));
            }
        } catch (SQLException e) {
            throw new IOException("Error retrieving user from database", e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Persoana> findAll() throws IOException {
        List<Persoana> persoane = new ArrayList<>();
        String sql = "SELECT * FROM persoane";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("persoanaId");
                String nume = resultSet.getString("nume");
                String prenume = resultSet.getString("prenume");
                String username = resultSet.getString("username");
                String parola = resultSet.getString("parola");
                Oras oras = Oras.valueOf(resultSet.getString("oras"));
                String strada = resultSet.getString("strada");
                String numarStrada = resultSet.getString("numarStrada");
                String telefon = resultSet.getString("telefon");
                persoane.add(new Persoana(id, nume, prenume, username, parola, oras, strada, numarStrada, telefon));
            }
        } catch (SQLException e) {
            throw new IOException("Error retrieving users from database", e);
        }
        return persoane;
    }

    @Override
    public Optional<Persoana> save(Persoana entity) throws IOException {
        String sql = "INSERT INTO persoane (nume, prenume, username, parola, oras, strada, numarStrada, telefon) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING persoanaId";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getNume());
            statement.setString(2, entity.getPrenume());
            statement.setString(3, entity.getUsername());
            statement.setString(4, entity.getParola());
            statement.setString(5, entity.getOras().toString());
            statement.setString(6, entity.getStrada());
            statement.setString(7, entity.getNumarStrada());
            statement.setString(8, entity.getTelefon());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                entity.setId(resultSet.getLong("persoanaId"));
                return Optional.of(entity);
            }
        } catch (SQLException e) {
            throw new IOException("Error saving user to database " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Persoana> delete(Long id) throws IOException {
        Optional<Persoana> entity = findOne(id);
        if (entity.isPresent()) {
            String sql = "DELETE FROM persoane WHERE persoanaId = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new IOException("Error removing user from database", e);
            }
        }
        return entity;
    }

    @Override
    public Optional<Persoana> update(Persoana entity) throws IOException {
        if(entity == null)
            throw new IllegalArgumentException("entity must be not null!");
        String sql = "update persoane set nume = ?, prenume = ?, username = ?, parola = ?, oras = ?, strada = ?, numarStrada = ?, telefon = ?  where persoanaId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, entity.getId());
            statement.setString(2, entity.getNume());
            statement.setString(3, entity.getPrenume());
            statement.setString(4, entity.getUsername());
            statement.setString(5, entity.getParola());
            statement.setString(6, entity.getOras().toString());
            statement.setString(7, entity.getStrada());
            statement.setString(8, entity.getNumarStrada());
            statement.setString(9, entity.getTelefon());
            if( statement.executeUpdate() > 0 )
                return Optional.empty();
            return Optional.of(entity);
        } catch (SQLException e) {
            throw new IOException("Error updating user in database", e);
        }
    }

}
