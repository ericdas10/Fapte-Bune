module examen.fapte_bune {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;

    exports examen.fapte_bune;
    exports examen.fapte_bune.gui;
    exports examen.fapte_bune.domain;
    opens examen.fapte_bune.gui to javafx.fxml;
    opens examen.fapte_bune.domain to javafx.base;
    opens examen.fapte_bune to javafx.graphics, javafx.fxml;
}