import 'reflect-metadata';
import { join } from 'path';
import { NestFactory } from '@nestjs/core';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { UserModule } from './user/user.module';

async function bootstrap(): Promise<void> {
  const app = await NestFactory.createMicroservice<MicroserviceOptions>(
    UserModule,
    {
      transport: Transport.GRPC,
      options: {
        package: 'user',
        protoPath: join(process.cwd(), 'dist/proto/user.proto'),
        url: '0.0.0.0:50052',
      },
    },
  );

  await app.listen();
}

void bootstrap();
