# Recurrent Invoice Payment Service

This projects holds the mandatory pieces to realize a basic recurrent invoice payment process.

Our billing service has the pending invoices already created so it's responsibility is to try to realize the payment.

## Ubiquitous Language

- Payment Provider: gateway between consumer and provider that allows multiple payment methods.
can be limited in some countries
  - have transactions fees
  - have change of currencies fees [Out of current problem]
  - have credit card fees
- Payment methods: ways to pay for a given resource, the most popular can be
  - card (Visa, MasterCard)
  - Digital Wallet
  - Paypal
  - Bank Transfer
- Invoice: A payment request
- Customer: Specific consumer using our services that pays a recurrent invoice


## Some Assumptions

We don't have to focus on the complexity of the Payment Provider, even when a real
payment provider usually has an asynchronous way to respond for example
webhooks, or you have to ask them recurrently the status of your transaction.

We will assume that the customer has already configured correctly a valid payment provider for his region,
and we have a provider key that relates this customer with the provider where all the
payment information is stored (if it was via credit card the number, expiration date, CVV...)

As in fact defined on the Payment Provider interface we would only allow transactions 
that has the same currency that found on the bank account, as realize currency conversion
it can be really difficult as the relation of currencies can change really fast

Our current system is a little one with few customers, so we can contain all the 
steps on a single process that will be launched every day to check pending payments

For next iterations about how to scalate the current application on the next section 
we show some system design that could help evolve it.

In our design we will guess that we only need to care about updating the invoice status directly, the customer
status or any collateral will be delegated via events so any listener can decide what to do.


## Some annotations

- If I self started the project some of the decisions that I will have taken differently will be:
  - Use a more functional approach for errors that will help avoid nulls on the repository layer (Arrow for kotlin with Either)
  - Use a hexagonal approach
- For a real project as we would have different projects the scheduler that has to be singleton to be run could be deployed using spring 
  scheduler or Quartz or even and probably the easier and more secure solution the own cron engine from the system 




