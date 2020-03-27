describe("Kjør enkel verdikjedetest på frontend", () => {
  it("Vis app og lag fagsak", () => {
    cy.visit("http://localhost:8000/");
    cy.get("input[name=username]").type("123456789");
    cy.get("button").click();
    cy.wait(1000);
  });
});
