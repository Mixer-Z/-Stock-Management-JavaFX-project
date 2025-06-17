package model.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class CommandeExterneLocal {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CommandeExterne commandeExterne;
    private Local local;

    // Constructors
    public CommandeExterneLocal() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public CommandeExterneLocal(CommandeExterne commandeExterne, Local local) {
        this();
        this.commandeExterne = commandeExterne;
        this.local = local;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CommandeExterne getCommandeExterne() {
        return commandeExterne;
    }

    public void setCommandeExterne(CommandeExterne commandeExterne) {
        this.commandeExterne = commandeExterne;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandeExterneLocal that = (CommandeExterneLocal) o;
        return Objects.equals(commandeExterne.getId(), that.commandeExterne.getId()) &&
                Objects.equals(local.getId(), that.local.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandeExterne.getId(), local.getId());
    }

    @Override
    public String toString() {
        return "CommandeExterneLocal{" +
                "commandeExterneId=" + (commandeExterne != null ? commandeExterne.getId() : "null") +
                ", localId=" + (local != null ? local.getId() : "null") +
                '}';
    }
}