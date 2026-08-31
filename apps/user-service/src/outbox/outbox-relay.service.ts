import {
  Injectable,
  Logger,
  OnModuleDestroy,
  OnModuleInit,
} from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';
import { Kafka, Producer } from 'kafkajs';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class OutboxRelayService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(OutboxRelayService.name);
  private readonly producer: Producer;

  constructor(private readonly prisma: PrismaService) {
    const kafka = new Kafka({
      clientId: 'user-service-outbox-relay',
      brokers: [process.env.KAFKA_BROKER ?? 'localhost:9092'],
    });

    this.producer = kafka.producer();
  }

  async onModuleInit(): Promise<void> {
    try {
      await this.producer.connect();
      this.logger.log('Kafka producer connected');
    } catch (error: unknown) {
      this.logger.error('Kafka connection failed', error);
    }
  }

  @Cron(CronExpression.EVERY_5_SECONDS)
  async relayPendingEvents(): Promise<void> {
    const events = await this.prisma.outboxEvent.findMany({
      where: {
        publishedAt: null,
      },
      orderBy: {
        createdAt: 'asc',
      },
    });

    for (const event of events) {
      try {
        await this.producer.send({
          topic: 'auth-events',
          messages: [
            {
              key: event.id,
              value: JSON.stringify(event.payload),
            },
          ],
        });

        await this.prisma.outboxEvent.update({
          where: {
            id: event.id,
          },
          data: {
            publishedAt: new Date(),
          },
        });

        this.logger.log(`Published outbox event ${event.id}`);
      } catch (error: unknown) {
        this.logger.error(
          `Failed to publish outbox event ${event.id}`,
          error,
        );
        break;
      }
    }
  }

  async onModuleDestroy(): Promise<void> {
    await this.producer.disconnect();
  }
}
