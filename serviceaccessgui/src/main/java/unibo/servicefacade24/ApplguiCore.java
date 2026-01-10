package unibo.servicefacade24;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.utils.CommUtils;

import java.util.*;

/*
Logica applicativa (domain core) della GUI
Creata da ServiceFacadeController usando FacadeBuilder
Sprint 3 - ColdStorageService
 */
public class ApplguiCore {
    private ActorOutIn outinadapter;
    private String destActor = "";

    public ApplguiCore(ActorOutIn outinadapter) {
        this.outinadapter = outinadapter;
        ApplSystemInfo.setup();
        destActor = ApplSystemInfo.applActorName; // "coldstorageservice"
    }

    // ========================================
    // UTILITY: Parsing Payload
    // ========================================

    /**
     * Estrae il payload da un messaggio formato msg(arg1, arg2, ...)
     * @param input Messaggio completo
     * @return Lista di argomenti
     */
    public static List<String> getPayload(String input) {
        List<String> resultList = new ArrayList<>();

        int startIndex = input.indexOf('(');
        int endIndex = input.lastIndexOf(')');

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String contentBetweenParentheses = input.substring(startIndex + 1, endIndex);
            String[] tokens = contentBetweenParentheses.split("\\s*,\\s*");

            for (String token : tokens) {
                resultList.add(token);
            }
        }

        return resultList;
    }

    // ========================================
    // GESTIONE MESSAGGI DA ATTORE (CoAP Observer)
    // ========================================

    /**
     * Chiamato da CoapObserver quando arriva un aggiornamento dalla risorsa osservata
     * @param msg Messaggio ricevuto
     * @param requestId ID richiesta (se presente)
     */
    public void handleMsgFromActor(String msg, String requestId) {
        CommUtils.outcyan("AGC | handleMsgFromActor " + msg + " requestId=" + requestId);

        // Invia TUTTI i messaggi alla GUI
        // La GUI (app.js) farà il parsing e deciderà cosa mostrare
        updateMsg(msg);
    }

    /**
     * Chiamato quando arriva una reply da una request TCP
     * @param msg Messaggio di risposta
     */
    public void handleReplyMsg(String msg) {
        CommUtils.outred("AGC | handleReplyMsg " + msg);
        updateMsg(msg);
    }

    /**
     * Invia messaggio alla GUI tramite WebSocket
     * @param msg Messaggio da inviare
     */
    public void updateMsg(String msg) {
        CommUtils.outblue("AGC | updateMsg " + msg);
        outinadapter.sendToAll(msg);
    }

    // ========================================
    // GESTIONE MESSAGGI DA GUI (WebSocket)
    // ========================================

    /**
     * Gestisce messaggi ricevuti dalla GUI via WebSocket
     * Formato: tipo/msgid/contenuto
     *
     * Esempi:
     * - request/storerequest/storerequest(50)
     * - request/ticketrequest/ticketrequest(1)
     * - requestInfo/
     * - exit/
     *
     * @param msg Messaggio dalla GUI
     */
    public void handleWsMsg(String msg) {
        CommUtils.outcyan("AGC | handleWsMsg " + msg);

        String[] parts = msg.split("/");
        String messageType = parts[0];
        String msgID = parts.length > 1 ? parts[1] : "";
        String msgContent = parts.length > 2 ? parts[2] : "";

        CommUtils.outcyan("AGC | Type: " + messageType + " | ID: " + msgID + " | Content: " + msgContent);

        switch (messageType) {
            case "request":
                // Format: request/storerequest/storerequest(50)
                // Format: request/ticketrequest/ticketrequest(1)
                dorequest(msgID, msgContent);
                break;

            case "cmd":
                docmd(msgContent);
                break;

            case "requestInfo":
                dorequestInfo();
                break;

            case "exit":
                System.exit(0);
                break;

            default:
                CommUtils.outred("AGC | Unknown message type: " + messageType);
                break;
        }
    }

    // ========================================
    // INVIO RICHIESTE AL SISTEMA QAK
    // ========================================

    /**
     * Invia una request al coldstorageservice
     * @param reqid ID della richiesta (es. "storerequest", "ticketrequest")
     * @param reqcontent Contenuto della richiesta (es. "storerequest(50)")
     */
    private void dorequest(String reqid, String reqcontent) {
        CommUtils.outgreen("AGC | dorequest reqid=" + reqid + " content=" + reqcontent);

        // Invia request via TCP al sistema QAK
        // Parametri: senderId, destActor, reqid, reqcontent
        outinadapter.dorequest("gui", destActor, reqid, reqcontent);
    }

    /**
     * Gestione comandi dispatch (non usato per ColdStorageService)
     * @param payload Payload del comando
     */
    private void docmd(String payload) {
        CommUtils.outgreen("AGC | docmd " + payload);
        // Non implementato per questo progetto
        // Se necessario in futuro, usare outinadapter.docmd(payload)
    }

    /**
     * Richiede informazioni sul sistema (attori presenti, ecc.)
     */
    private void dorequestInfo() {
        CommUtils.outgreen("AGC | dorequestInfo");

        List<String> actorNames = ApplSystemInfo.getActorNamesInApplCtx();

        String infoMsg = "ACTORS:" + actorNames.toString() +
                " | Interacting with: " + ApplSystemInfo.applActorName +
                " | Context: " + ApplSystemInfo.qakSysCtx +
                " | Port: " + ApplSystemInfo.ctxportStr;

        FacadeBuilder.wsHandler.sendToAll(infoMsg);
    }
}