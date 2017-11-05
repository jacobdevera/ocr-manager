package com.gjdevera.ocrreader.db;

public class Capture {
    private long id;
    private String text;
    private String created;

    public Capture(long id, String text, String created) {
        this.id = id;
        this.text = text;
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}
