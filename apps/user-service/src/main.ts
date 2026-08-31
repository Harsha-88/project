import 'dotenv/config';
import 'reflect-metadata';
import { readFileSync } from 'fs';
import { join } from 'path';
import { NestFactory } from '@nestjs/core';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { ServerCredentials } from '@grpc/grpc-js';
import { UserModule } from './user/user.module';

async function bootstrap(): Promise<void> {
  let ca: Buffer;
  let serverKey: Buffer;
  let serverCert: Buffer;

  try {
    const certDir = process.env.CERT_DIR
      ? join(process.env.CERT_DIR)
      : join(__dirname, '../../../../infrastructure/certs');

    ca = readFileSync(join(certDir, 'ca/ca.crt'));
    serverKey = readFileSync(join(certDir, 'user-service/server.key'));
    serverCert = readFileSync(join(certDir, 'user-service/server.crt'));
  } catch (error) {
    throw new Error(
      `Failed to load gRPC certificates. Check CERT_DIR or certificate files. ${
        error instanceof Error ? error.message : String(error)
      }`,
    );
  }

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
