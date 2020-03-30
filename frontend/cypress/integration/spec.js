describe("Kjør enkel verdikjedetest på frontend", () => {
  it("Vis app og lag fagsak", () => {
    cy.visit("http://host.docker.internal:8000/");
    cy.get("input[name=username]").type("123456789");
    cy.get("button").click();
    cy.get("#app").should("be.visible");

    cy.get(".skjemaelement__input").type("12345678901");
    cy.get(".knapp").click();
    cy.get(".fagsakcontainer__content--main").should("be.visible");
  });
});
