import { Module } from '@nestjs/common';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { readFileSync } from 'fs';
import { join } from 'path';
import { ChannelCredentials } from '@grpc/grpc-js';
import { AuthGrpcService } from './auth.grpc.service';
import { UserGrpcService } from './user.grpc.service';

const ca = readFileSync(
  join(process.cwd(), '../../infrastructure/certs/ca/ca.crt'),
);

const clientKey = readFileSync(
  join(process.cwd(), '../../infrastructure/certs/api-gateway/client.key'),
);

const clientCert = readFileSync(
  join(process.cwd(), '../../infrastructure/certs/api-gateway/client.crt'),
);

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
