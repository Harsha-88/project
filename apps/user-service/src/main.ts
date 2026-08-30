import 'dotenv/config';
import 'reflect-metadata';
import { readFileSync } from 'fs';
import { join } from 'path';
import { NestFactory } from '@nestjs/core';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { ServerCredentials } from '@grpc/grpc-js';
import { UserModule } from './user/user.module';

async function bootstrap(): Promise<void> {
  const ca = readFileSync(
    join(process.cwd(), '../../infrastructure/certs/ca/ca.crt'),
  );

  const serverKey = readFileSync(
    join(process.cwd(), '../../infrastructure/certs/user-service/server.key'),
  );

  const serverCert = readFileSync(
    join(process.cwd(), '../../infrastructure/certs/user-service/server.crt'),
  );

  const app = await NestFactory.createMicroservice<MicroserviceOptions>(
    UserModule,
    {
      transport: Transport.GRPC,
      options: {
        package: 'user',
        protoPath: join(process.cwd(), 'dist/proto/user.proto'),
        url: '0.0.0.0:50052',
        credentials: ServerCredentials.createSsl(
          ca,
          [
            {
              private_key: serverKey,
              cert_chain: serverCert,
            },
          ],
          true,
        ),
      },
    },
  );

  await app.listen();
}

void bootstrap();
