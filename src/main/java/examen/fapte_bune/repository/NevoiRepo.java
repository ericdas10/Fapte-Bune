package examen.fapte_bune.repository;

import examen.fapte_bune.domain.Nevoie;
import examen.fapte_bune.domain.Status;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NevoiRepo implements Repository<Long, Nevoie> {
    private final Connection connection;

    public NevoiRepo() throws SQLException {
        try{
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/faptebune","postgres","ericdas777");
            try (Statement statement = connection.createStatement()) {
                // Ensure the users table exists
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS nevoi(" +
                                "nevoieId SERIAL PRIMARY KEY," +
                                "titlu VARCHAR(50)," +
                                "descriere VARCHAR(150)," +
                                "deadline TIMESTAMP," +
                                "omInNevoie INT," +
                                "omSalvator INT," +
                                "status VARCHAR(40)," +
                                "FOREIGN KEY (omInNevoie) REFERENCES persoane(persoanaId)," +
                                "FOREIGN KEY (omSalvator) REFERENCES persoane(persoanaId));"

                );
            }

        }catch (SQLException e){
            throw new SQLException("Error initializing the database", e);
        }
    }



    @Override
    public Optional<Nevoie> findOne(Long id) throws IOException {
        String sql = "SELECT * FROM nevoi WHERE nevoieId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String titlu = resultSet.getString("titlu");
                String descriere = resultSet.getString("descriere");
                LocalDateTime deadline = resultSet.getTimestamp("deadline").toLocalDateTime();
                Long omInNevoie = resultSet.getLong("omInNevoie");
                Long omSalvator = resultSet.getLong("omSalvator");
                Status status = Status.valueOf(resultSet.getString("status"));
                return Optional.of(new Nevoie(id, titlu, descriere, deadline, omInNevoie, omSalvator, status));
            }
        } catch (SQLException e) {
            throw new IOException("Error retrieving nevoie from database", e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Nevoie> findAll() throws IOException {
        List<Nevoie> nevoi = new ArrayList<>();
        String sql = "SELECT * FROM nevoi";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("nevoieId");
                String titlu = resultSet.getString("titlu");
                String descriere = resultSet.getString("descriere");
                LocalDateTime deadline = resultSet.getTimestamp("deadline").toLocalDateTime();
                Long omInNevoie = resultSet.getLong("omInNevoie");
                Long omSalvator = resultSet.getLong("omSalvator");
                Status status = Status.valueOf(resultSet.getString("status"));
                nevoi.add(new Nevoie(id, titlu, descriere, deadline, omInNevoie, omSalvator, status));
            }
        } catch (SQLException e) {
            throw new IOException("Error retrieving nevoi from database", e);
        }
        return nevoi;
    }

    @Override
    public Optional<Nevoie> save(Nevoie entity) throws IOException {
        String sql = "INSERT INTO nevoi (titlu, descriere, deadline, omInNevoie, omSalvator, status) VALUES (?, ?, ?, ?, ?, ?) RETURNING nevoieId";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getTitlu());
            statement.setString(2, entity.getDescriere());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getDeadline()));
            statement.setLong(4, entity.getOmInNevoie());
            statement.setNull(5, Types.INTEGER);
            statement.setString(6, entity.getStatus().toString());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                entity.setId(resultSet.getLong("nevoieId"));
                return Optional.of(entity);
            }
        } catch (SQLException e) {
            throw new IOException("Error saving nevoie to database " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Nevoie> delete(Long id) throws IOException {
        Optional<Nevoie> entity = findOne(id);
        if (entity.isPresent()) {
            String sql = "DELETE FROM nevoi WHERE nevoieId = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new IOException("Error removing nevoie from database", e);
            }
        }
        return entity;
    }

    @Override
    public Optional<Nevoie> update(Nevoie entity) throws IOException {
        if(entity == null)
            throw new IllegalArgumentException("entity must be not null!");
        String sql = "update nevoi set titlu = ?, descriere = ?, deadline = ?, omInNevoie = ?, omSalvator = ?, status = ? where nevoieId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getTitlu());
            statement.setString(2, entity.getDescriere());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getDeadline()));
            statement.setLong(4, entity.getOmInNevoie());
            statement.setLong(5, entity.getOmSalvator());
            statement.setString(6, entity.getStatus().toString());
            statement.setLong(7, entity.getId());
            if( statement.executeUpdate() > 0 )
                return Optional.empty();
            return Optional.of(entity);
        } catch (SQLException e) {
            throw new IOException("Error updating nevoie in database", e);
        }
    }
}
