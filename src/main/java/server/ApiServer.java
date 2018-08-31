package server;

import entity.Account;
import entity.AccountDAO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import utils.UtilStrings;
import java.util.ArrayList;
import java.util.HashMap;

public class ApiServer extends AbstractVerticle {

    private ArrayList<Account> accountList;


    @Override
    public void start(Future<Void> fut){
        this.accountList = initialAccounts();
        Router router = Router.router(vertx);
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "text/html")
                    .end("<h1>GET /get/StringUUID POST /post/transaction /post/setAccount</h1>");
        });
        router.route(HttpMethod.POST, "/post/*").handler(BodyHandler.create());
        router.post("/post/transaction").handler(this::postTransaction);
        router.post("/post/setAccount").handler(this::createAccount);
        router.route(HttpMethod.GET, "/get/:id").handler(BodyHandler.create());
        router.get("/get/:id").handler(this::get);
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }

    private static ArrayList<Account> initialAccounts() {
        ArrayList<Account> initial = new ArrayList<Account>();
        for (int i = 0; i < 5; i++) {
            Account newAcc = new Account("Account " + i);
            newAcc.toString();
            initial.add(newAcc);
        }
        return initial;
    }

    private void postTransaction(RoutingContext ctx){
        HashMap<String,Object> transaction = Json.decodeValue(ctx.getBodyAsString(), HashMap.class);
        if (transaction == null) {
            ctx.response().setStatusCode(400).putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(UtilStrings.NO_DATA_TRANSACTION));
        } else {
            if ((transaction.get("receiver") != null || transaction.get("sender") != null || transaction.get("summ") != null) && transaction.get("summ").toString().matches("[0-9]+") ) {
                String receiverId = transaction.get("receiver").toString();
                String senderId = transaction.get("sender").toString();
                int summ = Integer.valueOf(transaction.get("summ").toString());
                Account receiver = new Account();
                Account sender = new Account();
                for (Account acc : accountList) {
                    if (acc.getId().equals(receiverId)) {
                        receiver = acc;
                        continue;
                    }
                    if (acc.getId().equals(senderId)) {
                        sender = acc;
                        continue;
                    }
                }
                if ((receiver.getId() != null || sender.getId() != null) && (sender.getFund() >= summ)) {
                    sender.setFund(sender.getFund() - summ);
                    receiver.setFund(receiver.getFund() + summ);
                    ArrayList<Account> results = new ArrayList<>();
                    results.add(sender);
                    results.add(receiver);
                    ctx.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(results));
                } else {
                    ctx.response().setStatusCode(400).putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(UtilStrings.NOT_ENOUGH_MONEY));
                }
            } else {
                ctx.response().setStatusCode(400).putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(UtilStrings.NO_DATA_TRANSACTION));
            }
        }
    }

    private void get(RoutingContext ctx){
        String id = ctx.request().getParam("id");
        if (id.equals("all")){
            ctx.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(accountList));
        }else{
            Account acc = AccountDAO.getAccount(id, accountList);
            ctx.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(acc));
        }
    }

    private void createAccount(RoutingContext ctx){
        HashMap<String,Object> createParams = Json.decodeValue(ctx.getBodyAsString(), HashMap.class);
        if (createParams == null) {
            ctx.response().setStatusCode(400).putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(UtilStrings.NO_DATA_TRANSACTION));
        } else {
            String name = createParams.get("name").toString();
            int fund = 0;
            if (createParams.get("fund").toString().matches("[0-9]+") && createParams.get("fund").toString().length() > 0) {
                fund = Integer.valueOf(createParams.get("fund").toString());
                if (!name.equals("")) {
                    Account newAcc = AccountDAO.createNewAcc(name, fund, accountList);
                    System.out.println("New entry was created: " + newAcc.toString());
                    ctx.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(newAcc));
                } else {
                    ctx.response().setStatusCode(400).putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(UtilStrings.CREATE_NONAME));
                }
            } else {
                ctx.response().setStatusCode(400).putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(UtilStrings.CREATE_BAD_DATA));
            }
        }
    }

}
