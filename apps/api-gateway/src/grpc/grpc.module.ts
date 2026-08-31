import { Module } from '@nestjs/common';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { readFileSync } from 'fs';
import { join } from 'path';
import { ChannelCredentials } from '@grpc/grpc-js';
import { AuthGrpcService } from './auth.grpc.service';
import { UserGrpcService } from './user.grpc.service';

let ca: Buffer;
let clientKey: Buffer;
let clientCert: Buffer;

try {
  const certDir = process.env.CERT_DIR
    ? join(process.env.CERT_DIR)
    : join(__dirname, '../../../../infrastructure/certs');

  ca = readFileSync(join(certDir, 'ca/ca.crt'));
  clientKey = readFileSync(join(certDir, 'api-gateway/client.key'));
  clientCert = readFileSync(join(certDir, 'api-gateway/client.crt'));
} catch (error) {
  throw new Error(
    `Failed to load gRPC certificates. Check CERT_DIR or certificate files. ${
      error instanceof Error ? error.message : String(error)
    }`,
  );
}

@Module({
  imports: [
    ClientsModule.register([
      {
        name: 'AUTH_GRPC',
        transport: Transport.GRPC,
        options: {
          package: 'auth',
          protoPath: join(__dirname, '../../../../proto/auth.proto'),
          url: process.env.AUTH_SERVICE_GRPC_URL || '127.0.0.1:50051',
          credentials: ChannelCredentials.createSsl(
            ca,
            clientKey,
            clientCert,
          ),
        },
      },
      {
        name: 'USER_GRPC',
        transport: Transport.GRPC,
        options: {
          package: 'user',
          protoPath: join(
            __dirname,
            '../../../user-service/src/proto/user.proto',
          ),
          url: process.env.USER_SERVICE_GRPC_URL || 'localhost:50052',
          credentials: ChannelCredentials.createSsl(
            ca,
            clientKey,
            clientCert,
          ),
        },
      },
    ]),
  ],
  providers: [AuthGrpcService, UserGrpcService],
  exports: [AuthGrpcService, UserGrpcService],
})
export class GrpcModule {}
