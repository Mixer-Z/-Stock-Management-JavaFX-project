package model.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class CommandeInterneLocal {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String notes; // Notes/comments about this specific location in the order

    private CommandeInterne commandeInterne;
    private Local local;

    // Constructors
    public CommandeInterneLocal() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public CommandeInterneLocal(CommandeInterne commandeInterne, Local local) {
        this.commandeInterne = commandeInterne;
        this.local = local;
        this.notes = "";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public CommandeInterneLocal(CommandeInterne commandeInterne, Local local, String notes) {
        this.commandeInterne = commandeInterne;
        this.local = local;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CommandeInterne getCommandeInterne() {
        return commandeInterne;
    }

    public void setCommandeInterne(CommandeInterne commandeInterne) {
        this.commandeInterne = commandeInterne;
        this.updatedAt = LocalDateTime.now();
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
        this.updatedAt = LocalDateTime.now();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
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
        CommandeInterneLocal that = (CommandeInterneLocal) o;
        return Objects.equals(commandeInterne.getId(), that.commandeInterne.getId()) &&
                Objects.equals(local.getId(), that.local.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandeInterne.getId(), local.getId());
    }

    @Override
    public String toString() {
        return "CommandeInterneLocal{" +
                "id=" + id +
                ", commandeInterneId=" + (commandeInterne != null ? commandeInterne.getId() : "null") +
                ", localId=" + (local != null ? local.getId() : "null") +
                ", localNom=" + (local != null ? local.getNom() : "null") +
                '}';
    }
}

