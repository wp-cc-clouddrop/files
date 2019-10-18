package com.clouddrop.files;

public interface IAdapter {

    /**
     * Lade eine Datei hoch
     * @return
     */
    public String ladeDateiHoch();

    /**
     * Aktualisiere eine Datei
     * @return
     */
    public String aktualisiereDatei();

    /**
     * Gib eine Datei an
     * @param id
     * @return
     */
    public String ladeDateiHerunter(Long id);

    /**
     * Loesche eine Datei
     * @param id
     * @return
     */
    public String loescheDatei(Long id);

    /**
     * Gib eine Liste der Dateien aus.
     * @return
     */
    public String gibListeVonDateien();

    /**
     * Suche nach einer bestimmten Datei
     * @param dateiName Dateiname
     * @param typ Typ
     * @param datum Datum
     * @return
     */
    public String sucheDatei(String dateiName, String typ, String datum);

}
