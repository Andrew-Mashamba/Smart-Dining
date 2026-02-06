<?php

namespace App\Http\Controllers;

use App\Services\StripePaymentService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;
use Stripe\Webhook;
use Stripe\Exception\SignatureVerificationException;
use Exception;

class StripeWebhookController extends Controller
{
    protected $stripeService;

    public function __construct(StripePaymentService $stripeService)
    {
        $this->stripeService = $stripeService;
    }

    /**
     * Handle incoming Stripe webhooks
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function handle(Request $request)
    {
        $payload = $request->getContent();
        $sigHeader = $request->header('Stripe-Signature');
        $webhookSecret = config('services.stripe.webhook_secret');

        try {
            // Verify webhook signature
            $event = Webhook::constructEvent(
                $payload,
                $sigHeader,
                $webhookSecret
            );
        } catch (SignatureVerificationException $e) {
            Log::error('Stripe webhook signature verification failed', [
                'error' => $e->getMessage(),
            ]);

            return response()->json(['error' => 'Invalid signature'], 400);
        } catch (Exception $e) {
            Log::error('Stripe webhook error', [
                'error' => $e->getMessage(),
            ]);

            return response()->json(['error' => 'Webhook error'], 400);
        }

        // Handle the event
        try {
            switch ($event->type) {
                case 'payment_intent.succeeded':
                    $this->handlePaymentIntentSucceeded($event->data->object);
                    break;

                case 'payment_intent.payment_failed':
                case 'payment_intent.failed':
                    $this->handlePaymentIntentFailed($event->data->object);
                    break;

                case 'payment_intent.processing':
                    $this->handlePaymentIntentProcessing($event->data->object);
                    break;

                case 'payment_intent.canceled':
                    $this->handlePaymentIntentCanceled($event->data->object);
                    break;

                default:
                    Log::info('Unhandled Stripe webhook event', [
                        'type' => $event->type,
                    ]);
            }

            return response()->json(['status' => 'success']);
        } catch (Exception $e) {
            Log::error('Failed to process Stripe webhook', [
                'event_type' => $event->type,
                'error' => $e->getMessage(),
            ]);

            return response()->json(['error' => 'Processing failed'], 500);
        }
    }

    /**
     * Handle successful payment intent
     *
     * @param object $paymentIntent
     * @return void
     */
    protected function handlePaymentIntentSucceeded($paymentIntent)
    {
        Log::info('Stripe payment succeeded', [
            'payment_intent_id' => $paymentIntent->id,
            'amount' => $paymentIntent->amount,
        ]);

        $this->stripeService->confirmPayment(
            $paymentIntent->id,
            [
                'status' => $paymentIntent->status,
                'amount' => $paymentIntent->amount,
                'currency' => $paymentIntent->currency,
                'payment_method' => $paymentIntent->payment_method,
                'charges' => $paymentIntent->charges->data ?? [],
            ]
        );
    }

    /**
     * Handle failed payment intent
     *
     * @param object $paymentIntent
     * @return void
     */
    protected function handlePaymentIntentFailed($paymentIntent)
    {
        Log::warning('Stripe payment failed', [
            'payment_intent_id' => $paymentIntent->id,
            'error' => $paymentIntent->last_payment_error->message ?? 'Unknown error',
        ]);

        $this->stripeService->failPayment(
            $paymentIntent->id,
            [
                'status' => $paymentIntent->status,
                'error' => $paymentIntent->last_payment_error ?? null,
            ]
        );
    }

    /**
     * Handle processing payment intent
     *
     * @param object $paymentIntent
     * @return void
     */
    protected function handlePaymentIntentProcessing($paymentIntent)
    {
        Log::info('Stripe payment processing', [
            'payment_intent_id' => $paymentIntent->id,
        ]);

        // Update payment status to processing if needed
        // This is optional - you may want to track this state
    }

    /**
     * Handle canceled payment intent
     *
     * @param object $paymentIntent
     * @return void
     */
    protected function handlePaymentIntentCanceled($paymentIntent)
    {
        Log::info('Stripe payment canceled', [
            'payment_intent_id' => $paymentIntent->id,
        ]);

        // Mark payment as canceled
        $this->stripeService->failPayment(
            $paymentIntent->id,
            [
                'status' => 'canceled',
                'canceled_at' => now()->toIso8601String(),
            ]
        );
    }
}
