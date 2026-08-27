import { Module } from '@nestjs/common';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { join } from 'path';
import { AuthGrpcService } from './auth.grpc.service';

@Module({
  imports: [
    ClientsModule.register([
      {
        name: 'AUTH_GRPC',
        transport: Transport.GRPC,
        options: {
          package: 'auth',
          protoPath: join(__dirname, '../../../../proto/auth.proto'),
          url: process.env.USER_SERVICE_GRPC_URL || 'localhost:50051',
        },
      },
    ]),
  ],
  providers: [AuthGrpcService],
  exports: [AuthGrpcService],
})
export class GrpcModule {}
