package com.example.test.spring;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.util.concurrent.ThreadLocalRandom;

@Route("")
@StyleSheet("styles/styles.css")
@Push
@PWA(name = "Vaadin Chat", shortName = "Chat")
public class MainView extends VerticalLayout {

  private final UnicastProcessor<ChatMessage> publisher;
  private final Flux<ChatMessage> messages;
  private String username;
  private final String ADMIN = "admin";

  public MainView(UnicastProcessor<ChatMessage> publisher, Flux<ChatMessage> messages) {
    this.publisher = publisher;
    this.messages = messages;
    addClassName("main-view");
    setSizeFull();
    setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    H1 header = new H1("Friends Chat");
    header.getElement().getThemeList().add("dark");
    add(header);

    askUsername();
    getThemeList().add("dark");
  }

  private void askUsername() {
    HorizontalLayout layout = new HorizontalLayout();

    TextField usernameField = new TextField();
    usernameField.setPlaceholder("username");
    Button startButton = new Button("Start chatting");

    startButton.addClickListener(click -> {
      username = usernameField.getValue();
      remove(layout);
      startChat();
    });

    layout.add(usernameField, startButton);

    add(layout);
  }

  private void startChat() {
    MessageList messageList = new MessageList(); // Div

    Button btnClearChat = new Button("clear");
    btnClearChat.addClickListener(click -> {
      messageList.removeAll(); // clear chat
    });


    add(messageList, createInputLayout());
    if(username.equals(ADMIN)){
      add(btnClearChat);
    }

    String [] chatColors = {"p-red", "p-black", "p-yellow", "p-pink", "p-green"};

    messages.subscribe(m -> {
      getUI().ifPresent(ui -> ui.access(() ->
              {
                Paragraph paragraph = new Paragraph(m.getFrom() + ": " + m.getMessage());
                int randomNum = ThreadLocalRandom.current().nextInt(0, 5);
                paragraph.addClassName(chatColors[randomNum]);
                messageList.add(paragraph);
              }

      ));
    });



    expand(messageList);


  }


  private Component createInputLayout() {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setWidth("100%");

    TextField messageField = new TextField();
    Button send = new Button("Send");

    send.setThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName());
    send.addClickShortcut(Key.ENTER);
    send.addClickListener(click -> {
      if(!messageField.getValue().trim().isEmpty()){
        publisher.onNext(new ChatMessage(username, messageField.getValue()));
        messageField.clear();
        messageField.focus();
      }
    });


    layout.add(messageField, send);
    layout.expand(messageField);
    return layout;
  }

}
