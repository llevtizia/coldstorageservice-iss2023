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

    // --------------------------------------------
    // UTILITY: Parsing Payload
    // --------------------------------------------

    /**
     * estrae il payload da un messaggio formato msg(arg1, arg2, ...)
     * input -> messaggio completo
     * return -> lista di argomenti
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

    // --------------------------------------------
    // GESTIONE MESSAGGI DA ATTORE (CoAP Observer)
    // --------------------------------------------

    /**
     * chiamato da CoapObserver quando arriva un aggiornamento dalla risorsa osservata
     * msg -> messaggio ricevuto
     * requestId -> ID richiesta (se presente)
     */
    public void handleMsgFromActor(String msg, String requestId) {
        CommUtils.outcyan("AGC | handleMsgFromActor " + msg + " requestId=" + requestId);

        // invia TUTTI i messaggi alla GUI
        // la GUI (app.js) farà il parsing e deciderà cosa mostrare
        updateMsg(msg);
    }

    /**
     * chiamato quando arriva una reply da una request TCP
     * msg -> messaggio di risposta
     */
    public void handleReplyMsg(String msg) {
        CommUtils.outred("AGC | handleReplyMsg " + msg);
        updateMsg(msg);
    }

    /**
     * invia messaggio alla GUI tramite WebSocket
     * msg -> messaggio da inviare
     */
    public void updateMsg(String msg) {
        CommUtils.outblue("AGC | updateMsg " + msg);
        outinadapter.sendToAll(msg);
    }

    // --------------------------------------------
    // GESTIONE MESSAGGI DA GUI (WebSocket)
    // --------------------------------------------

    /**
     * gestisce messaggi ricevuti dalla GUI via WebSocket
     * formato: tipo/msgid/contenuto
     *
     * esempi:
     * - request/storerequest/storerequest(50)
     * - request/ticketrequest/ticketrequest(1)
     * - requestInfo/
     * - exit/
     *
     * msg -> messaggio dalla GUI
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
                // format: request/storerequest/storerequest(50)
                // format: request/ticketrequest/ticketrequest(1)
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

    // --------------------------------------------
    // INVIO RICHIESTE AL SISTEMA QAK
    // --------------------------------------------

    /**
     * invia una request al coldstorageservice
     * reqid -> ID della richiesta (es. "storerequest", "ticketrequest")
     * reqcontent -> contenuto della richiesta (es. "storerequest(50)")
     */
    private void dorequest(String reqid, String reqcontent) {
        CommUtils.outgreen("AGC | dorequest reqid=" + reqid + " content=" + reqcontent);

        // Invia request via TCP al sistema QAK
        // Parametri: senderId, destActor, reqid, reqcontent
        outinadapter.dorequest("gui", destActor, reqid, reqcontent);
    }

    /**
     * gestione comandi dispatch (non usato per ColdStorageService)
     * payload -> payload del comando
     */
    private void docmd(String payload) {
        CommUtils.outgreen("AGC | docmd " + payload);
        // non implementato per questo progetto
        // outinadapter.docmd(payload)
    }

    /**
     * richiede informazioni sul sistema (attori presenti, ecc.)
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